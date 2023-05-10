package ru.kotomore.telegramservice.telegram.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.kotomore.telegramservice.enums.DefinitionEnum;
import ru.kotomore.telegramservice.enums.EntityEnum;
import ru.kotomore.telegramservice.models.TelegramUser;
import ru.kotomore.telegramservice.repositories.TelegramUserRepository;
import ru.kotomore.telegramservice.messaging.RabbitMessageSender;
import ru.kotomore.telegramservice.services.UserAwaitingService;
import telegram.TelegramMessage;

@Component
@RequiredArgsConstructor
public class CallbackQueryHandler {
    private static final String ENTER_SERVICE_NAME = "Введите название услуги";
    private static final String ENTER_SERVICE_DESCRIPTION = "Введите описание услуги";
    private static final String ENTER_SERVICE_PRICE = "Введите цену услуги";
    private static final String ENTER_SERVICE_DURATION = "Введите длительность услуги в минутах";
    private static final String ENTER_USER_NAME = "Введите Ф.И.О";
    private static final String ENTER_USER_PASSWORD = "Введите пароль";

    private final UserAwaitingService userAwaitingService;
    private final TelegramUserRepository repository;
    private final RabbitMessageSender rabbitMessageSender;

    public BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery) {
        final String chatId = buttonQuery.getMessage().getChatId().toString();
        final Integer messageId = buttonQuery.getMessage().getMessageId();

        String data = buttonQuery.getData();

        if (data.contains("APPOINTMENT_DELETE")) {
            String appointmentId = data.replace("APPOINTMENT_DELETE", "");
            TelegramUser telegramUser = repository.findByChatId(chatId).orElseThrow(() -> new RuntimeException("User not found"));

            rabbitMessageSender.deleteAppointment(telegramUser.getAgentId(), appointmentId);
            return new DeleteMessage(chatId, buttonQuery.getMessage().getMessageId());
        }

        switch (data) {
            case "SERVICE_NAME":
                SendMessage nameMessage = new SendMessage(chatId, ENTER_SERVICE_NAME);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.SERVICE_, DefinitionEnum.NAME);
                nameMessage.setReplyToMessageId(messageId);
                return nameMessage;
            case "SERVICE_DESCRIPTION":
                SendMessage descriptionMessage = new SendMessage(chatId, ENTER_SERVICE_DESCRIPTION);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.SERVICE_, DefinitionEnum.DESCRIPTION);
                descriptionMessage.setReplyToMessageId(messageId);
                return descriptionMessage;
            case "SERVICE_PRICE":
                SendMessage priceMessage = new SendMessage(chatId, ENTER_SERVICE_PRICE);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.SERVICE_, DefinitionEnum.PRICE);
                priceMessage.setReplyToMessageId(messageId);
                return priceMessage;
            case "SERVICE_DURATION":
                SendMessage durationMessage = new SendMessage(chatId, ENTER_SERVICE_DURATION);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.SERVICE_, DefinitionEnum.DURATION);
                durationMessage.setReplyToMessageId(messageId);
                return durationMessage;


            //Agent info
            case "AGENT_NAME":
                SendMessage agentNameMessage = new SendMessage(chatId, ENTER_USER_NAME);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.AGENT_, DefinitionEnum.NAME);
                agentNameMessage.setReplyToMessageId(messageId);
                return agentNameMessage;
            case "AGENT_PASSWORD":
                SendMessage agentPasswordMessage = new SendMessage(chatId, ENTER_USER_PASSWORD);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.AGENT_, DefinitionEnum.PASSWORD);
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
                        `2023-12-30
                        2023-12-30
                        09:00
                        18:00`""";

                SendMessage agentSchedule = new SendMessage(chatId, message);
                agentSchedule.enableMarkdown(true);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.SCHEDULE_, DefinitionEnum.TIME);
                agentSchedule.setReplyToMessageId(messageId);
                return agentSchedule;

            case "SCHEDULE_DELETE":
                deleteSchedule(chatId);
                return null;

            default:
                return null;
        }
    }

    private void deleteSchedule(String chatId) {
        String agentId = repository.findByChatId(chatId).orElseThrow().getAgentId();
        rabbitMessageSender.sendTelegramMessage(agentId, TelegramMessage.Action.SCHEDULE_DELETE);
    }
}