package ru.set404.telegramservice.telegram.handlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.set404.telegramservice.enums.CallbackActionDefinitionEnum;
import ru.set404.telegramservice.enums.CallbackActionPartsEnum;
import ru.set404.telegramservice.services.WaitAnswerService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    private final WaitAnswerService waitAnswerService;

    public BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery) {
        final String chatId = buttonQuery.getMessage().getChatId().toString();
        final Integer messageId = buttonQuery.getMessage().getMessageId();

        String data = buttonQuery.getData();

        switch (data) {
            case "SERVICE_NAME":
                SendMessage nameMessage = new SendMessage(chatId, ENTER_SERVICE_NAME);
                waitAnswerService.addToWaitingList(chatId, CallbackActionPartsEnum.SERVICE_, CallbackActionDefinitionEnum.NAME);
                nameMessage.setReplyToMessageId(messageId);
                return nameMessage;
            case "SERVICE_DESCRIPTION":
                SendMessage descriptionMessage = new SendMessage(chatId, ENTER_SERVICE_DESCRIPTION);
                waitAnswerService.addToWaitingList(chatId, CallbackActionPartsEnum.SERVICE_, CallbackActionDefinitionEnum.DESCRIPTION);
                descriptionMessage.setReplyToMessageId(messageId);
                return descriptionMessage;
            case "SERVICE_PRICE":
                SendMessage priceMessage = new SendMessage(chatId, ENTER_SERVICE_PRICE);
                waitAnswerService.addToWaitingList(chatId, CallbackActionPartsEnum.SERVICE_, CallbackActionDefinitionEnum.PRICE);
                priceMessage.setReplyToMessageId(messageId);
                return priceMessage;
            case "SERVICE_DURATION":
                SendMessage durationMessage = new SendMessage(chatId, ENTER_SERVICE_DURATION);
                waitAnswerService.addToWaitingList(chatId, CallbackActionPartsEnum.SERVICE_, CallbackActionDefinitionEnum.DURATION);
                durationMessage.setReplyToMessageId(messageId);
                return durationMessage;


                //Agent info
            case "INFO_NAME":
                SendMessage agentNameMessage = new SendMessage(chatId, ENTER_USER_NAME);
                waitAnswerService.addToWaitingList(chatId, CallbackActionPartsEnum.AGENT_, CallbackActionDefinitionEnum.NAME);
                agentNameMessage.setReplyToMessageId(messageId);
                return agentNameMessage;
            case "INFO_PASSWORD":
                SendMessage agentPasswordMessage = new SendMessage(chatId, ENTER_USER_PASSWORD);
                waitAnswerService.addToWaitingList(chatId, CallbackActionPartsEnum.AGENT_, CallbackActionDefinitionEnum.PASSWORD);
                agentPasswordMessage.setReplyToMessageId(messageId);
                return agentPasswordMessage;

                //Schedule
            case "SCHEDULE_TIME":
                String message = """
                        Введите время одним сообщением в формате:
                        Дата начала-----> 2023-12-30 09:00 <-Ежедневное время начала
                        Дата окончания-> 2023-12-30 18:00 <-Ежедневное время окончания



                        Пример:
                        *2023-12-30 09:00
                        2023-12-30 18:00*""";

                SendMessage agentSchedule = new SendMessage(chatId, message);
                agentSchedule.enableMarkdown(true);
                waitAnswerService.addToWaitingList(chatId, CallbackActionPartsEnum.SCHEDULE_, CallbackActionDefinitionEnum.TIME);
                agentSchedule.setReplyToMessageId(messageId);
                return agentSchedule;
            default:
                return null;
        }
    }
}