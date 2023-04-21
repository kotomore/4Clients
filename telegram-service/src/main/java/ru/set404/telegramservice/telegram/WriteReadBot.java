package ru.set404.telegramservice.telegram;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import ru.set404.telegramservice.telegram.handlers.CallbackQueryHandler;
import ru.set404.telegramservice.telegram.handlers.TelegramMessageHandler;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WriteReadBot extends SpringWebhookBot {
    String botPath;
    String botUsername;
    String botToken;
    TelegramMessageHandler telegramMessageHandler;
    CallbackQueryHandler callbackQueryHandler;

    public WriteReadBot(SetWebhook setWebhook, String botToken, TelegramMessageHandler telegramMessageHandler, CallbackQueryHandler callbackQueryHandler) {
        super(setWebhook, botToken);
        this.telegramMessageHandler = telegramMessageHandler;
        this.callbackQueryHandler = callbackQueryHandler;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        try {
            return handleUpdate(update);
        } catch (IllegalArgumentException e) {
            return new SendMessage(update.getMessage().getChatId().toString(),
                    "Illegal message");
        } catch (Exception e) {
            return new SendMessage(update.getMessage().getChatId().toString(),
                    "Not accepted");
        }
    }

    private BotApiMethod<?> handleUpdate(Update update) throws InterruptedException {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            return callbackQueryHandler.processCallbackQuery(callbackQuery);
        } else {
            Message message = update.getMessage();
            if (message != null) {
                if (message.hasContact()) {
                    telegramMessageHandler.processRegistration(message);
                } else {
                    return telegramMessageHandler.answerMessage(update.getMessage());
                }
            }
        }
        return null;
    }
}
