package ru.set404.telegramservice.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.set404.telegramservice.models.TelegramUser;
import ru.set404.telegramservice.telegram.WriteReadBot;
import ru.set404.telegramservice.telegram.keyboards.ReplyKeyboardMaker;

@Service
@AllArgsConstructor
@Slf4j
public class MessageService {
    private ReplyKeyboardMaker replyKeyboardMaker;
    private WriteReadBot writeReadBot;

    public void sendSuccessRegMessage(TelegramUser user) {
        SendMessage sendMessage = new SendMessage(user.getChatId(), "Регистрация завершена\nВыберите пункт меню:");
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(replyKeyboardMaker.getMainMenuKeyboard());
        try {
            writeReadBot.execute(sendMessage);
        } catch (TelegramApiException tAe) {
            log.debug(tAe.getMessage());
        }
    }

    public void sendInvalidRegMessage(TelegramUser user) {
        SendMessage sendMessage = new SendMessage(user.getChatId(), "Ошибка регистрации, попробуйте снова:");
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(replyKeyboardMaker.getSingleButtonKeyboard("Регистрация"));
        try {
            writeReadBot.execute(sendMessage);
        } catch (TelegramApiException tAe) {
            log.debug(tAe.getMessage());
        }
    }
}
