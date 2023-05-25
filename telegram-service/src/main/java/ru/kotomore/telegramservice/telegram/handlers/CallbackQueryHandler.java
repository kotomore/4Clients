package ru.kotomore.telegramservice.telegram.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.kotomore.telegramservice.enums.DefinitionEnum;
import ru.kotomore.telegramservice.enums.EntityEnum;
import ru.kotomore.telegramservice.models.TelegramUser;
import ru.kotomore.telegramservice.repositories.TelegramUserRepository;
import ru.kotomore.telegramservice.messaging.RabbitMessageSender;
import ru.kotomore.telegramservice.services.UserAwaitingService;
import ru.kotomore.telegramservice.telegram.keyboards.InlineKeyboardMaker;
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
    private final InlineKeyboardMaker inlineKeyboardMaker;

    public BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery) {
        final String chatId = buttonQuery.getMessage().getChatId().toString();
        final Integer messageId = buttonQuery.getMessage().getMessageId();

        String data = buttonQuery.getData();

        switch (data) {
            case "SERVICE_NAME" -> {
                SendMessage nameMessage = new SendMessage(chatId, ENTER_SERVICE_NAME);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.SERVICE_, DefinitionEnum.NAME);
                nameMessage.setReplyToMessageId(messageId);
                return nameMessage;
            }
            case "SERVICE_DESCRIPTION" -> {
                SendMessage descriptionMessage = new SendMessage(chatId, ENTER_SERVICE_DESCRIPTION);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.SERVICE_, DefinitionEnum.DESCRIPTION);
                descriptionMessage.setReplyToMessageId(messageId);
                return descriptionMessage;
            }
            case "SERVICE_PRICE" -> {
                SendMessage priceMessage = new SendMessage(chatId, ENTER_SERVICE_PRICE);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.SERVICE_, DefinitionEnum.PRICE);
                priceMessage.setReplyToMessageId(messageId);
                return priceMessage;
            }
            case "SERVICE_DURATION" -> {
                SendMessage durationMessage = new SendMessage(chatId, ENTER_SERVICE_DURATION);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.SERVICE_, DefinitionEnum.DURATION);
                durationMessage.setReplyToMessageId(messageId);
                return durationMessage;
            }

            //Agent info
            case "AGENT_NAME" -> {
                SendMessage agentNameMessage = new SendMessage(chatId, ENTER_USER_NAME);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.AGENT_, DefinitionEnum.NAME);
                agentNameMessage.setReplyToMessageId(messageId);
                return agentNameMessage;
            }
            case "AGENT_PASSWORD" -> {
                SendMessage agentPasswordMessage = new SendMessage(chatId, ENTER_USER_PASSWORD);
                userAwaitingService.addToWaitingList(chatId, EntityEnum.AGENT_, DefinitionEnum.PASSWORD);
                agentPasswordMessage.setReplyToMessageId(messageId);
                return agentPasswordMessage;
            }

            //Schedule
            case "SCHEDULE_TIME" -> {
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
            }

            case "SCHEDULE_DELETE_ALL" -> {
                EditMessageText editScheduleMessage = new EditMessageText();
                editScheduleMessage.setChatId(chatId);
                editScheduleMessage.setMessageId(messageId);
                editScheduleMessage.setText("Очистить всё расписание?");
                editScheduleMessage.setReplyMarkup(inlineKeyboardMaker.getConfirmInlineButton(EntityEnum.SCHEDULE_));
                return editScheduleMessage;
            }

            case "SCHEDULE_DELETE_ALL_CONFIRMED" -> {
                actionMessage(chatId, TelegramMessage.Action.SCHEDULE_DELETE);
                DeleteMessage deleteScheduleMessage = new DeleteMessage();
                deleteScheduleMessage.setChatId(chatId);
                deleteScheduleMessage.setMessageId(messageId);
                return deleteScheduleMessage;
            }

            case "SCHEDULE_NEXT_PAGE" -> {
                EditMessageText editMessageText = createEditMessage(chatId, messageId,
                        inlineKeyboardMaker.getScheduleInlineButton(true),
                        userAwaitingService.getNextMessageFromCache(chatId, EntityEnum.SCHEDULE_));
                editMessageText.enableHtml(true);
                return editMessageText;
            }

            case "SCHEDULE_PREV_PAGE" -> {
                EditMessageText editMessageText1 = createEditMessage(chatId, messageId,
                        inlineKeyboardMaker.getScheduleInlineButton(true),
                        userAwaitingService.getPreviousMessageFromCache(chatId, EntityEnum.SCHEDULE_));
                editMessageText1.enableHtml(true);
                return editMessageText1;
            }

            case "APPOINTMENT_DELETE_ALL" -> {
                EditMessageText editAppointmentMessage = new EditMessageText();
                editAppointmentMessage.setChatId(chatId);
                editAppointmentMessage.setMessageId(messageId);
                editAppointmentMessage.setText("Удалить все записи?");
                editAppointmentMessage.setReplyMarkup(inlineKeyboardMaker.getConfirmInlineButton(EntityEnum.APPOINTMENT_));
                return editAppointmentMessage;
            }

            case "APPOINTMENT_DELETE_ALL_CONFIRMED" -> {
                actionMessage(chatId, TelegramMessage.Action.APPOINTMENTS_DELETE);
                return new DeleteMessage(chatId, messageId);
            }

            case "APPOINTMENT_NEXT_PAGE" -> {
                return createEditMessage(chatId, messageId, inlineKeyboardMaker.getAppointmentInlineButton(true),
                        userAwaitingService.getNextMessageFromCache(chatId, EntityEnum.APPOINTMENT_));
            }

            case "APPOINTMENT_PREV_PAGE" -> {
                return createEditMessage(chatId, messageId, inlineKeyboardMaker.getAppointmentInlineButton(true),
                        userAwaitingService.getPreviousMessageFromCache(chatId, EntityEnum.APPOINTMENT_));
            }
        }
        if (data.contains("APPOINTMENT_DELETE")) {
            String appointmentId = data.replace("APPOINTMENT_DELETE", "");
            TelegramUser telegramUser = repository.findByChatId(chatId).orElseThrow(() -> new RuntimeException("User not found"));

            rabbitMessageSender.deleteAppointment(telegramUser.getAgentId(), appointmentId);
            return new DeleteMessage(chatId, buttonQuery.getMessage().getMessageId());
        }
        return null;
    }

    private EditMessageText createEditMessage(String chatId, int messageId, InlineKeyboardMarkup inlineKeyboardMarkup,
                                              String text) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        editMessageText.enableMarkdown(true);
        return editMessageText;
    }

    private void actionMessage(String chatId, TelegramMessage.Action action) {
        String agentId = repository.findByChatId(chatId).orElseThrow().getAgentId();
        rabbitMessageSender.sendTelegramMessage(agentId, action);
    }
}