package ru.set404.telegramservice.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.set404.telegramservice.dto.telegram.AgentMSG;
import ru.set404.telegramservice.dto.telegram.AgentServiceMSG;
import ru.set404.telegramservice.dto.telegram.AppointmentMSG;
import ru.set404.telegramservice.dto.telegram.ScheduleMSG;
import ru.set404.telegramservice.models.TelegramUser;
import ru.set404.telegramservice.telegram.WriteReadBot;
import ru.set404.telegramservice.telegram.keyboards.InlineKeyboardMaker;
import ru.set404.telegramservice.telegram.keyboards.ReplyKeyboardMaker;

@Service
@AllArgsConstructor
@Slf4j
public class MessageService {
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
            String builder = "*Дата*: " + appointmentMSG.getDate() + "\n" +
                    "*Клиент:*\n" +
                    "    Имя: " + appointmentMSG.getClient().getName() + "\n" +
                    "    Телефон: " + appointmentMSG.getClient().getPhone() + "\n" +
                    "*Время*:\n" +
                    "    Начало: " + appointmentMSG.getStartTime() + "\n" +
                    "    Окончание: " + appointmentMSG.getEndTime();

            SendMessage sendMessage = new SendMessage(user.getChatId(), builder);
            sendMessage.enableMarkdown(true);
            try {
                writeReadBot.execute(sendMessage);
            } catch (TelegramApiException tAe) {
                log.debug(tAe.getMessage());
            }
        }
    }

    public void sendAgentSchedule(TelegramUser user, ScheduleMSG scheduleMSG) {
        if (user.getChatId() != null) {
            String answer = "*Период работы:* " + (scheduleMSG.getDateStart() == null ?
                    "Не задано" :
                    (scheduleMSG.getDateStart() + " - " + scheduleMSG.getDateEnd())) + "\n" +

                    "*Часы работы:* " + (scheduleMSG.getTimeStart() == null ?
                    "Не задано" :
                    (scheduleMSG.getTimeStart() + " - " + scheduleMSG.getTimeEnd())) + "\n";

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
