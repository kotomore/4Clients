package ru.set404.telegramservice.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.set404.telegramservice.models.TelegramUser;
import ru.set404.telegramservice.repositories.TelegramUserRepository;
import ru.set404.telegramservice.telegram.WriteReadBot;
import ru.set404.telegramservice.telegram.keyboards.InlineKeyboardMaker;
import ru.set404.telegramservice.telegram.keyboards.ReplyKeyboardMaker;
import telegram.*;

import java.time.LocalDate;
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

    public void sendAgentAppointmentsMessage(List<AppointmentMSG> appointmentMSGS) {
        String chatId = appointmentMSGS.isEmpty() ? null : getChatId(appointmentMSGS.get(0).getAgentId());
        if (chatId != null) {

            StringBuilder messageBuilder = new StringBuilder();
            LocalDate date = LocalDate.MIN;

            for (AppointmentMSG appointmentMSG : appointmentMSGS) {
                messageBuilder.append(appointmentMSG.getType() == AppointmentMSG.Type.NEW ? "*Новая заявка:*\n\n" : "");
                if (!date.equals(appointmentMSG.getDate())) {
                    messageBuilder.append("\n\n*____ ").append(appointmentMSG.getDate()).append(" ____*\n\n");
                    date = appointmentMSG.getDate();
                }

                messageBuilder
                        .append("*Время*: ").append(appointmentMSG.getStartTime()).append(" - ")
                        .append(appointmentMSG.getEndTime()).append("\n")
                        .append("*Имя*: ").append(appointmentMSG.getClientName()).append("\n")
                        .append("*Телефон*: `").append(appointmentMSG.getClientPhone()).append("`")
                        .append("\n\n");
            }

            SendMessage sendMessage = new SendMessage(chatId, messageBuilder.toString());
            sendMessage.enableMarkdown(true);
            try {
                writeReadBot.execute(sendMessage);
            } catch (TelegramApiException tAe) {
                log.debug(tAe.getMessage());
            }
        }
    }

    public void sendAgentSchedule(AvailabilityMSG availabilityMSG) {
        String chatId = getChatId(availabilityMSG.getAgentId());
        if (chatId != null) {
            String answer;
            if (availabilityMSG.getAvailabilities() != null) {
                StringBuilder availabilities = new StringBuilder();
                LocalDate date = LocalDate.MIN;
                for (Availability availability : availabilityMSG.getAvailabilities()) {
                    if (!date.equals(availability.getDate())) {
                        availabilities.append("\n*").append("Дата: ").append(availability.getDate()).append("*\n\n");
                        date = availability.getDate();
                    }
                    availabilities.append(formatAvailabilityTime(availability));
                }

                answer = availabilities.toString();
            } else {
                answer = "";
            }

            if (answer.isEmpty()) {
                answer = "Расписание не задано";
            }

            SendMessage sendMessage = new SendMessage(chatId, answer);
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(inlineKeyboardMaker.getScheduleInlineButton());
            try {
                writeReadBot.execute(sendMessage);
            } catch (TelegramApiException tAe) {
                log.debug(tAe.getMessage());
            }
        }
    }

    private String formatAvailabilityTime(Availability availability) {
        StringBuilder formattedTime = new StringBuilder();
        if (availability.isBooked()) {
            formattedTime.append(String.format("*%s - %s* - Запись\n", availability.getStartTime(), availability.getEndTime()));
        } else {
            formattedTime.append(String.format("%s - %s\n", availability.getStartTime(), availability.getEndTime()));
        }
        return formattedTime.toString();
    }

    private String getChatId(String agentId) {
        TelegramUser user = repository.findByAgentId(agentId).orElse(new TelegramUser());
        return user.getChatId();
    }

}
