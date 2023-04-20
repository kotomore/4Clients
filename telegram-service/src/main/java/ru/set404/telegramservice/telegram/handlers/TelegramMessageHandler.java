package ru.set404.telegramservice.telegram.handlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.set404.telegramservice.models.TelegramUser;
import ru.set404.telegramservice.repositories.TelegramUserRepository;
import ru.set404.telegramservice.services.RabbitService;
import ru.set404.telegramservice.telegram.keyboards.InlineKeyboardMaker;
import ru.set404.telegramservice.telegram.keyboards.ReplyKeyboardMaker;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TelegramMessageHandler {

    ReplyKeyboardMaker replyKeyboardMaker;
    InlineKeyboardMaker inlineKeyboardMaker;
    RabbitService rabbitService;
    TelegramUserRepository repository;


    public BotApiMethod<?> answerMessage(Message message) {
        String chatId = message.getChatId().toString();

        String inputText = message.getText();

        if (inputText == null) {
            throw new IllegalArgumentException();
        } else if (inputText.equals("/start")) {
            return getRegMessage(chatId);
        } else if (inputText.equals("m")) {
            return getTestMessage(chatId);
        } else if (inputText.equals("Расписание")) {
            return sendScheduleMessage(chatId);
        } else {
            return null;
        }
    }

    public void processRegistration(Message message) {
        String chatId = message.getChatId().toString();
        String phone = message.getContact().getPhoneNumber();

        TelegramUser user = repository.findByPhone(phone).orElseGet(TelegramUser::new);
        user.setPhone(phone);
        user.setChatId(chatId);
        repository.save(user);
        rabbitService.registerAgentByPhone(phone);
    }

    private SendMessage getStartMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(chatId, "Выберите пункт меню:");
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(replyKeyboardMaker.getMainMenuKeyboard());
        return sendMessage;
    }

    private SendMessage getTestMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(chatId, "Test");
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(inlineKeyboardMaker.getInlineMessageButtons("123", true));
        return sendMessage;
    }

    private SendMessage getRegMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(chatId, "Нажмите кнопку регистрация");
        sendMessage.enableMarkdown(true);
        ReplyKeyboardMaker maker = new ReplyKeyboardMaker();

        sendMessage.setReplyMarkup(maker.getSingleButtonKeyboard("Регистрация"));


        return sendMessage;
    }

    private SendMessage sendScheduleMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(chatId, "Нажмите ");
        sendMessage.setReplyMarkup(inlineKeyboardMaker.getInlineMessageButtons(
                "Callbac",
                true
        ));
        return sendMessage;
    }
}
