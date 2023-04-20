package ru.set404.telegramservice.telegram;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import ru.set404.telegramservice.telegram.handlers.TelegramMessageHandler;

@Getter
@Setter
public class WriteReadBot extends SpringWebhookBot {
    String botPath;
    String botUsername;
    String botToken;

    TelegramMessageHandler telegramMessageHandler;

    public WriteReadBot(SetWebhook setWebhook, String botToken, TelegramMessageHandler telegramMessageHandler) {
        super(setWebhook, botToken);
        this.telegramMessageHandler = telegramMessageHandler;
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

    private BotApiMethod<?> handleUpdate(Update update) {
        Message message = update.getMessage();
        if (message != null) {
            if (message.hasContact()) {
                telegramMessageHandler.processRegistration(message);
            } else {
                return telegramMessageHandler.answerMessage(update.getMessage());
            }
        }
        return null;
    }

}
