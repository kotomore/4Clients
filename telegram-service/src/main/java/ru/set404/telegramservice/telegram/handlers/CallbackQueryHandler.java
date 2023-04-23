package ru.set404.telegramservice.telegram.handlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.set404.telegramservice.constants.ActionDefinitionEnum;
import ru.set404.telegramservice.constants.ActionPartEnum;
import ru.set404.telegramservice.services.UserAwaitingService;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CallbackQueryHandler {
    private static final String ENTER_SERVICE_NAME = "Введите название услуги";
    private static final String ENTER_SERVICE_DESCRIPTION = "Введите описание услуги";
    private static final String ENTER_SERVICE_PRICE = "Введите цену услуги";
    private static final String ENTER_SERVICE_DURATION = "Введите длительность услуги";
    private static final String ENTER_USER_NAME = "Введите Ф.И.О";
    private static final String ENTER_USER_PASSWORD = "Введите пароль";

    private final UserAwaitingService userAwaitingService;

    public BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery) {
        final String chatId = buttonQuery.getMessage().getChatId().toString();
        final Integer messageId = buttonQuery.getMessage().getMessageId();

        String data = buttonQuery.getData();

        switch (data) {
            case "SERVICE_NAME":
                SendMessage nameMessage = new SendMessage(chatId, ENTER_SERVICE_NAME);
                userAwaitingService.addToWaitingList(chatId, ActionPartEnum.SERVICE_, ActionDefinitionEnum.NAME);
                nameMessage.setReplyToMessageId(messageId);
                return nameMessage;
            case "SERVICE_DESCRIPTION":
                SendMessage descriptionMessage = new SendMessage(chatId, ENTER_SERVICE_DESCRIPTION);
                userAwaitingService.addToWaitingList(chatId, ActionPartEnum.SERVICE_, ActionDefinitionEnum.DESCRIPTION);
                descriptionMessage.setReplyToMessageId(messageId);
                return descriptionMessage;
            case "SERVICE_PRICE":
                SendMessage priceMessage = new SendMessage(chatId, ENTER_SERVICE_PRICE);
                userAwaitingService.addToWaitingList(chatId, ActionPartEnum.SERVICE_, ActionDefinitionEnum.PRICE);
                priceMessage.setReplyToMessageId(messageId);
                return priceMessage;
            case "SERVICE_DURATION":
                SendMessage durationMessage = new SendMessage(chatId, ENTER_SERVICE_DURATION);
                userAwaitingService.addToWaitingList(chatId, ActionPartEnum.SERVICE_, ActionDefinitionEnum.DURATION);
                durationMessage.setReplyToMessageId(messageId);
                return durationMessage;


                //Agent info
            case "INFO_NAME":
                SendMessage agentNameMessage = new SendMessage(chatId, ENTER_USER_NAME);
                userAwaitingService.addToWaitingList(chatId, ActionPartEnum.AGENT_, ActionDefinitionEnum.NAME);
                agentNameMessage.setReplyToMessageId(messageId);
                return agentNameMessage;
            case "INFO_PASSWORD":
                SendMessage agentPasswordMessage = new SendMessage(chatId, ENTER_USER_PASSWORD);
                userAwaitingService.addToWaitingList(chatId, ActionPartEnum.AGENT_, ActionDefinitionEnum.PASSWORD);
                agentPasswordMessage.setReplyToMessageId(messageId);
                return agentPasswordMessage;

                //Schedule
            case "SCHEDULE_TIME":
                String message = """
                        Введите дату и время одним сообщением в формате:
                        *Дата начала:* 2023-12-30
                        *Дата окончания:* 2023-12-30
                        *Ежедневное время начала:* 09:00
                        *Ежедневное время окончания:* 18:00


                        Пример:
                        *2023-12-30
                        2023-12-30
                        09:00
                        18:00*""";

                SendMessage agentSchedule = new SendMessage(chatId, message);
                agentSchedule.enableMarkdown(true);
                userAwaitingService.addToWaitingList(chatId, ActionPartEnum.SCHEDULE_, ActionDefinitionEnum.TIME);
                agentSchedule.setReplyToMessageId(messageId);
                return agentSchedule;
            default:
                return null;
        }
    }
}