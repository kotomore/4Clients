package ru.kotomore.telegramservice.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kotomore.telegramservice.enums.EntityEnum;
import ru.kotomore.telegramservice.models.TelegramUser;
import ru.kotomore.telegramservice.repositories.TelegramUserRepository;
import ru.kotomore.telegramservice.telegram.WriteReadBot;
import ru.kotomore.telegramservice.telegram.keyboards.InlineKeyboardMaker;
import ru.kotomore.telegramservice.telegram.keyboards.ReplyKeyboardMaker;
import telegram.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class TelegramMessageService {
    private TelegramUserRepository repository;
    private ReplyKeyboardMaker replyKeyboardMaker;
    private InlineKeyboardMaker inlineKeyboardMaker;
    private WriteReadBot writeReadBot;
    private UserAwaitingService userAwaitingService;

    public void registerUser(AgentMSG agentMSG) {

        Optional<TelegramUser> user = repository.findByPhone(agentMSG.getPhone());
        if (user.isPresent()) {
            user.get().setAgentId(agentMSG.getId());
            repository.save(user.get());

            SendMessage sendMessage = new SendMessage(user.get().getChatId(), "Регистрация завершена\n*Выберите пункт меню*");
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(replyKeyboardMaker.getMainMenuKeyboard());

            try {
                writeReadBot.execute(sendMessage);
            } catch (TelegramApiException tAe) {
                log.debug(tAe.getMessage());
            }
        }
    }

    public void sendErrorMessage(ErrorMSG errorMSG) {
        String chatId = getChatId(errorMSG.getAgentId());
        SendMessage sendMessage = new SendMessage(chatId, errorMSG.getMessage());
        try {
            writeReadBot.execute(sendMessage);
        } catch (TelegramApiException tAe) {
            log.debug(tAe.getMessage());
        }
    }

    public void sendAgentServiceMessage(AgentServiceMSG service) {
        String chatId = getChatId(service.getAgentId());
        if (chatId != null) {
            String answer = "*Название:* " + (service.getName() == null ? "" : service.getName()) + "\n" +
                    "*Описание:* " + (service.getDescription() == null ? "" : service.getDescription()) + "\n" +
                    "*Длительность:* " + (service.getDuration() == 0 ? "" : service.getDuration()) + " мин.\n" +
                    "*Цена:* " + (service.getPrice() == 0d ? "" : service.getPrice()) + " руб.";

            SendMessage sendMessage = new SendMessage(chatId, answer);
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(inlineKeyboardMaker.getServiceInlineButton());
            try {
                writeReadBot.execute(sendMessage);
            } catch (TelegramApiException tAe) {
                log.debug(tAe.getMessage());
            }
        }
    }

    public void sendAgentInfoMessage(AgentMSG agentMSG) {
        String chatId = getChatId(agentMSG.getId());
        if (chatId != null) {
            String answer = "*Ф.И.О.:* " + (agentMSG.getName() == null ? "" : agentMSG.getName());
            SendMessage sendMessage = new SendMessage(chatId, answer);
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(inlineKeyboardMaker.getAgentInlineButton());
            try {
                writeReadBot.execute(sendMessage);
            } catch (TelegramApiException tAe) {
                log.debug(tAe.getMessage());
            }
        }
    }

    public boolean sendAgentAppointmentsMessage(List<AppointmentMSG> appointmentMSGS) {
        if (appointmentMSGS.isEmpty()) {
            return false;
        }

        String chatId = getChatId(appointmentMSGS.get(0).getAgentId());
        if (chatId == null || chatId.isEmpty()) {
            return false;
        }

        StringBuilder messageBuilder = new StringBuilder();
        List<String> appointmentMessages = new ArrayList<>();
        LocalDate date = null;
        String appointmentId = null;
        for (AppointmentMSG appointmentMSG : appointmentMSGS) {
            if (date == null || !date.equals(appointmentMSG.getDate())) {
                if (date != null) {
                    appointmentMessages.add(messageBuilder.toString());
                    messageBuilder = new StringBuilder();
                }
                date = appointmentMSG.getDate();
                messageBuilder.append("\n\n*____ ").append(date).append(" ____*\n\n");
            }

            if (appointmentMSG.getType() == AppointmentMSG.Type.NEW) {
                messageBuilder.append("*Новая заявка:*\n\n");
                appointmentId = appointmentMSG.getAppointmentId();
            }

            messageBuilder
                    .append("*Время*: ").append(appointmentMSG.getStartTime()).append("- ")
                    .append(appointmentMSG.getEndTime()).append("\n")
                    .append("*Имя*: ").append(appointmentMSG.getClientName()).append("\n")
                    .append("*Телефон*: `").append(appointmentMSG.getClientPhone()).append("`\n\n");
        }
        appointmentMessages.add(messageBuilder.toString());

        return sendAppointmentMessage(chatId, appointmentId, appointmentMessages);
    }

    private boolean sendAppointmentMessage(String chatId, String appointmentId, List<String> appointmentMessages) {
        SendMessage sendMessage = new SendMessage(chatId, appointmentMessages.get(0));
        sendMessage.enableMarkdown(true);

        if (appointmentId != null) {
            sendMessage.setReplyMarkup(inlineKeyboardMaker.getAppointmentDeleteInlineButton(appointmentId));
        } else {
            sendMessage.setReplyMarkup(inlineKeyboardMaker.getAppointmentInlineButton(appointmentMessages.size() > 1));
        }

        if (appointmentMessages.size() > 1) {
            userAwaitingService.addMessageToCache(chatId, EntityEnum.APPOINTMENT_, appointmentMessages);
        }

        try {
            writeReadBot.execute(sendMessage);
            return true;
        } catch (TelegramApiException tAe) {
            log.debug(tAe.getMessage());
            return false;
        }
    }


    public void sendAgentSchedule(AvailabilityMSG availabilityMSG) {
        String chatId = getChatId(availabilityMSG.getAgentId());
        if (chatId == null) {
            return;
        }

        userAwaitingService.clearUserCache(chatId, EntityEnum.SCHEDULE_);
        List<String> availabilityMSGCache = new ArrayList<>();

        StringBuilder answerBuilder = new StringBuilder();

        List<Availability> availabilities = availabilityMSG.getAvailabilities();
        if (availabilities != null && !availabilities.isEmpty()) {
            LocalDate date = null;

            LocalDate firstDate = availabilities.get(0).getDate();
            LocalDate lastDate = availabilities.get(availabilities.size() - 1).getDate();
            String dates = "\n\n" + firstDate + " - " + lastDate;

            for (Availability availability : availabilities) {
                if (date == null || !date.equals(availability.getDate())) {
                    if (date != null) {
                        answerBuilder.append(dates).append("</pre>");
                        availabilityMSGCache.add(answerBuilder.toString());
                        answerBuilder = new StringBuilder();
                    }
                    date = availability.getDate();
                    answerBuilder.append("<b>Дата: ")
                            .append(date)
                            .append("</b><pre>\n\n\n");
                }
                answerBuilder.append(formatAvailabilityTime(availability));
            }
            answerBuilder.append(dates).append("</pre>");
            availabilityMSGCache.add(answerBuilder.toString());
        }

        sendScheduleMessage(chatId, availabilityMSGCache);
    }

    private void sendScheduleMessage(String chatId, List<String> availabilityMSGCache) {
        String answer;
        if (!availabilityMSGCache.isEmpty()) {
            answer = availabilityMSGCache.get(0);
        } else {
            answer = "Расписание не задано";
        }

        SendMessage sendMessage = new SendMessage(chatId, answer);
        sendMessage.enableHtml(true);
        if (availabilityMSGCache.size() > 1) {
            userAwaitingService.addMessageToCache(chatId, EntityEnum.SCHEDULE_, availabilityMSGCache);
            sendMessage.setReplyMarkup(inlineKeyboardMaker.getScheduleInlineButton(true));
        } else {
            sendMessage.setReplyMarkup(inlineKeyboardMaker.getScheduleInlineButton(false));
        }

        try {
            writeReadBot.execute(sendMessage);
        } catch (TelegramApiException tAe) {
            log.debug(tAe.getMessage());
        }
    }

    private String formatAvailabilityTime(Availability availability) {
        if (availability.isBooked()) {
            return String.format("%s - %s - Запись\n", availability.getStartTime(), availability.getEndTime());
        } else {
            return String.format("%s - %s\n", availability.getStartTime(), availability.getEndTime());
        }
    }

    private String getChatId(String agentId) {
        TelegramUser user = repository.findByAgentId(agentId).orElse(new TelegramUser());
        return user.getChatId();
    }

}
