package ru.set404.telegramservice.telegram.handlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.set404.telegramservice.constants.ActionDefinitionEnum;
import ru.set404.telegramservice.constants.ActionPartEnum;
import ru.set404.telegramservice.dto.telegram.AgentMSG;
import ru.set404.telegramservice.dto.telegram.AgentServiceMSG;
import ru.set404.telegramservice.dto.telegram.ScheduleMSG;
import ru.set404.telegramservice.dto.telegram.TelegramMessage;
import ru.set404.telegramservice.models.TelegramUser;
import ru.set404.telegramservice.models.UserAwaitingResponse;
import ru.set404.telegramservice.repositories.TelegramUserRepository;
import ru.set404.telegramservice.services.RabbitService;
import ru.set404.telegramservice.services.UserAwaitingService;
import ru.set404.telegramservice.telegram.keyboards.ReplyKeyboardMaker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TelegramMessageHandler {
    RabbitService rabbitService;
    TelegramUserRepository repository;
    UserAwaitingService userAwaitingService;

    public BotApiMethod<?> answerMessage(Message message) {
        String chatId = message.getChatId().toString();
        String inputText = message.getText();

        switch (inputText) {
            case "/start":
                return getRegMessage(chatId);
            case "Редактирование услуг":
                Optional<TelegramUser> user = repository.findByChatId(chatId);
                user.ifPresent(telegramUser -> rabbitService.sendTelegramMessage(telegramUser.getAgentId(), TelegramMessage.Action.SERVICE_INFO));
                return null;
            case "Личные данные":
                user = repository.findByChatId(chatId);
                user.ifPresent(telegramUser -> rabbitService.sendTelegramMessage(telegramUser.getAgentId(), TelegramMessage.Action.AGENT_INFO));
                return null;
            case "Расписание":
                user = repository.findByChatId(chatId);
                user.ifPresent(telegramUser -> rabbitService.sendTelegramMessage(telegramUser.getAgentId(), TelegramMessage.Action.SCHEDULES));
                return null;
            case "Записи":
                user = repository.findByChatId(chatId);
                user.ifPresent(telegramUser -> rabbitService.sendTelegramMessage(telegramUser.getAgentId(), TelegramMessage.Action.APPOINTMENTS));
                return null;
            case "Код для сайта":
                user = repository.findByChatId(chatId);
                if (user.isPresent()) {
                    try {
                        //if run in docker container path = "/root/frontend.html"
                        Path path = Paths.get("telegram-service/src/main/resources/frontend.html");
                        if (!Files.exists(path)) {
                            path = Paths.get("/root/frontend.html");
                        }

                        String text = String.join("\n", Files.readString(path));
                        text = "`" + text.replace("${therapistId}", user.get().getAgentId()) + "`";

                        String msg = """
                                *Для добавления формы записи на сайт вставьте следующий код в нужный раздел вашего сайта.*
                                Нажмите на код чтобы скопировать:


                                """ + text;

                        SendMessage sendMessage = new SendMessage(chatId, msg);
                        sendMessage.enableMarkdown(true);
                        return sendMessage;
                    } catch (IOException e) {
                        return null;
                    }
                }
                return null;
        }

        if (userAwaitingService.contains(chatId)) {
            UserAwaitingResponse userAwaitingResponse = userAwaitingService.getWaiter(chatId);
            ActionPartEnum actionPart = userAwaitingResponse.actionPart();
            ActionDefinitionEnum definition = userAwaitingResponse.definition();

            if (actionPart == ActionPartEnum.SERVICE_) {
                if (definition == ActionDefinitionEnum.NAME) {
                    return updateAgentServiceName(message, chatId);
                } else if (definition == ActionDefinitionEnum.DESCRIPTION) {
                    return updateAgentServiceDescription(message, chatId);
                } else if (definition == ActionDefinitionEnum.DURATION) {
                    return updateAgentServiceDuration(message, chatId);
                } else if (definition == ActionDefinitionEnum.PRICE) {
                    return updateAgentServicePrice(message, chatId);
                }
            }

            if (actionPart == ActionPartEnum.AGENT_) {
                if (definition == ActionDefinitionEnum.NAME) {
                    return updateAgentName(message, chatId);
                } else if (definition == ActionDefinitionEnum.PASSWORD) {
                    return updateAgentPassword(message, chatId);
                }
            }

            if (actionPart == ActionPartEnum.SCHEDULE_) {
                return updateAgentSchedule(message, chatId);
            }
        }
        return null;
    }

    private BotApiMethod<?> updateAgentServiceName(Message message, String chatId) {
        AgentServiceMSG service = new AgentServiceMSG();
        service.setName(message.getText());
        return updateService(chatId, service);
    }

    private BotApiMethod<?> updateAgentServiceDescription(Message message, String chatId) {
        AgentServiceMSG service = new AgentServiceMSG();
        service.setDescription(message.getText());
        return updateService(chatId, service);
    }

    private BotApiMethod<?> updateAgentServiceDuration(Message message, String chatId) {
        AgentServiceMSG service = new AgentServiceMSG();
        try {
            int duration = Integer.parseInt(message.getText());
            service.setDuration(duration);
            return updateService(chatId, service);
        } catch (NumberFormatException exception) {
            return new SendMessage(chatId, "Введите длительность услуги");
        }
    }

    private BotApiMethod<?> updateAgentServicePrice(Message message, String chatId) {
        AgentServiceMSG service = new AgentServiceMSG();
        try {
            double price = Double.parseDouble(message.getText());
            service.setPrice(price);
            return updateService(chatId, service);
        } catch (NumberFormatException exception) {
            return new SendMessage(chatId, "Введите стоимость услуги");
        }
    }

    private BotApiMethod<?> updateService(String chatId, AgentServiceMSG service) {
        Optional<TelegramUser> user = repository.findByChatId(chatId);
        if (user.isPresent()) {
            service.setAgentId(user.get().getAgentId());
            rabbitService.updateService(service);
            userAwaitingService.removeFromWaitingList(chatId);
        }
        return null;
    }

    private BotApiMethod<?> updateAgentName(Message message, String chatId) {
        AgentMSG agentMSG = new AgentMSG();
        agentMSG.setName(message.getText());
        return updateAgent(chatId, agentMSG);
    }

    private BotApiMethod<?> updateAgentPassword(Message message, String chatId) {
        AgentMSG agentMSG = new AgentMSG();
        agentMSG.setPassword(message.getText());
        return updateAgent(chatId, agentMSG);
    }

    private BotApiMethod<?> updateAgent(String chatId, AgentMSG agentMSG) {
        Optional<TelegramUser> user = repository.findByChatId(chatId);
        if (user.isPresent()) {
            agentMSG.setId(user.get().getAgentId());
            rabbitService.updateAgent(agentMSG);
            userAwaitingService.removeFromWaitingList(chatId);
        }
        return null;
    }

    private BotApiMethod<?> updateAgentSchedule(Message message, String chatId) {
        ScheduleMSG scheduleMSG = new ScheduleMSG();

        String[] messages = message.getText().split("\n");
        if (messages.length == 4) {
            try {
                scheduleMSG.setAgentId(message.getText());
                scheduleMSG.setDateStart(LocalDate.parse(messages[0]));
                scheduleMSG.setDateEnd(LocalDate.parse(messages[1]));
                scheduleMSG.setTimeStart(LocalTime.parse(messages[2]));
                scheduleMSG.setTimeEnd(LocalTime.parse(messages[3]));

            } catch (DateTimeParseException ex) {
                return new SendMessage(chatId, "Введите дату и время в указанном формате");
            }

            return updateSchedule(chatId, scheduleMSG);
        }
        return new SendMessage(chatId, "Введите дату и время в указанном формате");
    }

    private BotApiMethod<?> updateSchedule(String chatId, ScheduleMSG schedule) {
        Optional<TelegramUser> user = repository.findByChatId(chatId);
        if (user.isPresent()) {
            schedule.setAgentId(user.get().getAgentId());
            rabbitService.updateSchedule(schedule);
            userAwaitingService.removeFromWaitingList(chatId);
        }
        return null;
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

    private SendMessage getRegMessage(String chatId) {
        String text = """
                *4Clients* – сервис онлайн записи клиентов, предназначен для сферы услуг. Обеспечивает ведение клиентской базы и удобную запись для клиентов. Виджет можно установить на сайт или дать клиенту прямую ссылку.

                *Для входа или регистрации нажмите кнопку ниже.*""";
        SendMessage sendMessage = new SendMessage(chatId, text);
        sendMessage.enableMarkdown(true);
        ReplyKeyboardMaker maker = new ReplyKeyboardMaker();

        sendMessage.setReplyMarkup(maker.getSingleButtonKeyboard("Регистрация"));

        return sendMessage;
    }
}
