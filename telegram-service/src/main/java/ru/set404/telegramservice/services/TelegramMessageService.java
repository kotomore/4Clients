package ru.set404.telegramservice.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.set404.telegramservice.dto.telegram.*;
import ru.set404.telegramservice.models.TelegramUser;
import ru.set404.telegramservice.telegram.WriteReadBot;
import ru.set404.telegramservice.telegram.keyboards.InlineKeyboardMaker;
import ru.set404.telegramservice.telegram.keyboards.ReplyKeyboardMaker;

import java.time.LocalDate;

@Service
@AllArgsConstructor
@Slf4j
public class TelegramMessageService {
    private ReplyKeyboardMaker replyKeyboardMaker;
    private InlineKeyboardMaker inlineKeyboardMaker;
    private WriteReadBot writeReadBot;

    public void sendSuccessRegMessage(TelegramUser user) {
        SendMessage sendMessage = new SendMessage(user.getChatId(), "Регистрация завершена\n*Выберите пункт меню*");
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(replyKeyboardMaker.getMainMenuKeyboard());
        try {
            writeReadBot.execute(sendMessage);
        } catch (TelegramApiException tAe) {
            log.debug(tAe.getMessage());
        }
    }

    public void sendAgentServiceMessage(TelegramUser user, AgentServiceMSG service) {
        if (user.getChatId() != null) {
            String answer = "*Название:* " + (service.getName() == null ? "" : service.getName()) + "\n" +
                    "*Описание:* " + (service.getDescription() == null ? "" : service.getDescription()) + "\n" +
                    "*Длительность:* " + (service.getDuration() == 0 ? "" : service.getDuration()) + " мин.\n" +
                    "*Цена:* " + (service.getPrice() == 0d ? "" : service.getPrice()) + " руб.";

            SendMessage sendMessage = new SendMessage(user.getChatId(), answer);
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(inlineKeyboardMaker.getServiceInlineButton());
            try {
                writeReadBot.execute(sendMessage);
            } catch (TelegramApiException tAe) {
                log.debug(tAe.getMessage());
            }
        }
    }

    public void sendAgentInfoMessage(TelegramUser user, AgentMSG agentMSG) {
        if (user.getChatId() != null) {
            String answer = "*Ф.И.О.:* " + (agentMSG.getName() == null ? "" : agentMSG.getName());
            SendMessage sendMessage = new SendMessage(user.getChatId(), answer);
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(inlineKeyboardMaker.getAgentInlineButton());
            try {
                writeReadBot.execute(sendMessage);
            } catch (TelegramApiException tAe) {
                log.debug(tAe.getMessage());
            }
        }
    }

    public void sendAgentAppointmentsMessage(TelegramUser user, AppointmentMSG appointmentMSG) {
        if (user.getChatId() != null) {
            String text = (appointmentMSG.getType() == AppointmentMSG.Type.NEW ?
                    "*Новая заявка:*\n\n" : "") +

                    "*Дата*: " + appointmentMSG.getDate() + "\n" +
                    "*Время*:\n" +
                    "    Начало: " + appointmentMSG.getStartTime() + "\n" +
                    "    Окончание: " + appointmentMSG.getEndTime() + "\n" +
                    "*Клиент:*\n" +
                    "    Имя: " + appointmentMSG.getClientName() + "\n" +
                    "    Телефон: `" + appointmentMSG.getClientPhone() + "`";

            SendMessage sendMessage = new SendMessage(user.getChatId(), text);
            sendMessage.enableMarkdown(true);
            try {
                writeReadBot.execute(sendMessage);
            } catch (TelegramApiException tAe) {
                log.debug(tAe.getMessage());
            }
        }
    }

    public void sendAgentSchedule(TelegramUser user, AvailabilityMSG availabilityMSG) {
        if (user.getChatId() != null) {

            StringBuilder availabilities = new StringBuilder();
            LocalDate date = LocalDate.MIN;
            for (Availability availability : availabilityMSG.getAvailabilities()) {
                if (!date.equals(availability.getDate())) {
                    availabilities.append("\n*").append("Дата: ").append(availability.getDate()).append("*\n\n");
                    date = availability.getDate();
                }
                availabilities.append(availability.getStartTime()).append(" - ").append(availability.getEndTime()).append("\n");
            }

            String answer = availabilities.toString();

            if (answer.isEmpty()) {
                answer = "Расписание не задано";
            }

            SendMessage sendMessage = new SendMessage(user.getChatId(), answer);
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(inlineKeyboardMaker.getScheduleInlineButton());
            try {
                writeReadBot.execute(sendMessage);
            } catch (TelegramApiException tAe) {
                log.debug(tAe.getMessage());
            }
        }
    }

}
