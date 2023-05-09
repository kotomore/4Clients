package ru.kotomore.telegramservice.telegram.handlers;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.kotomore.telegramservice.constants.ActionDefinitionEnum;
import ru.kotomore.telegramservice.constants.ActionPartEnum;
import ru.kotomore.telegramservice.models.TelegramUser;
import ru.kotomore.telegramservice.models.UserAwaitingResponse;
import ru.kotomore.telegramservice.repositories.TelegramUserRepository;
import ru.kotomore.telegramservice.services.RabbitService;
import ru.kotomore.telegramservice.services.UserAwaitingService;
import ru.kotomore.telegramservice.telegram.keyboards.ReplyKeyboardMaker;
import telegram.AgentMSG;
import telegram.AgentServiceMSG;
import telegram.ScheduleMSG;
import telegram.TelegramMessage;

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
public class TelegramMessageHandler {
    RabbitService rabbitService;
    TelegramUserRepository repository;
    UserAwaitingService userAwaitingService;
    String siteUrl;

    public TelegramMessageHandler(RabbitService rabbitService, TelegramUserRepository repository, UserAwaitingService userAwaitingService, @Value("${site.url}") String siteUrl) {
        this.rabbitService = rabbitService;
        this.repository = repository;
        this.userAwaitingService = userAwaitingService;
        this.siteUrl = siteUrl;
    }

    public BotApiMethod<?> answerMessage(Message message) {
        String chatId = message.getChatId().toString();
        String inputText = message.getText();

        switch (inputText) {
            case "/start":
                userAwaitingService.removeFromWaitingList(chatId);
                return getRegMessage(chatId);
            case "Редактирование услуг":
                userAwaitingService.removeFromWaitingList(chatId);
                Optional<TelegramUser> user = repository.findByChatId(chatId);
                if (user.isPresent()) {
                    rabbitService.sendTelegramMessage(user.get().getAgentId(), TelegramMessage.Action.SERVICE_INFO);
                    break;
                } else {
                    return sendNeedAuthMessage(chatId);
                }

            case "Личные данные":
                userAwaitingService.removeFromWaitingList(chatId);
                user = repository.findByChatId(chatId);
                if (user.isPresent()) {
                    rabbitService.sendTelegramMessage(user.get().getAgentId(), TelegramMessage.Action.AGENT_INFO);
                    break;
                } else {
                    return sendNeedAuthMessage(chatId);
                }
            case "Расписание":
                userAwaitingService.removeFromWaitingList(chatId);
                user = repository.findByChatId(chatId);
                if (user.isPresent()) {
                    rabbitService.sendTelegramMessage(user.get().getAgentId(), TelegramMessage.Action.SCHEDULES);
                    break;
                } else {
                    return sendNeedAuthMessage(chatId);
                }
            case "Записи":
                userAwaitingService.removeFromWaitingList(chatId);
                user = repository.findByChatId(chatId);
                if (user.isPresent()) {
                    rabbitService.sendTelegramMessage(user.get().getAgentId(), TelegramMessage.Action.APPOINTMENTS);
                    break;
                } else {
                    return sendNeedAuthMessage(chatId);
                }

            case "Код для сайта":
                userAwaitingService.removeFromWaitingList(chatId);
                user = repository.findByChatId(chatId);
                if (user.isPresent()) {
                    try {
                        //if run in docker container path = "/root/frontend.html"
                        Path path = Paths.get("telegram-service/src/main/resources/frontend.html");
                        if (!Files.exists(path)) {
                            path = Paths.get("/root/frontend.html");
                        }

                        String text = String.join("\n", Files.readString(path));
                        text = "`" + text.replace("${agentId}", user.get().getAgentId()) + "`";
                        text = text.replace("${siteUrl}", siteUrl);

                        String msg = """
                                *Для добавления формы записи на сайт вставьте следующий код в нужный раздел вашего сайта.*
                                Нажмите на код чтобы скопировать:


                                """ + text + "\n\n\n\nВаша персональная ссылка: " + siteUrl + "/" + user.get().getAgentId();

                        SendMessage sendMessage = new SendMessage(chatId, msg);
                        sendMessage.enableMarkdown(true);
                        return sendMessage;
                    } catch (IOException e) {
                        break;
                    }
                } else {
                    return sendNeedAuthMessage(chatId);
                }
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
                if (definition == ActionDefinitionEnum.TIME) {
                    return updateAgentSchedule(message, chatId);
                }
            }
        }
        return null;
    }

    private SendMessage sendNeedAuthMessage(String chatId) {
        String text = "*Необходим вход в приложение*";
        SendMessage sendMessage = new SendMessage(chatId, text);
        sendMessage.enableMarkdown(true);
        ReplyKeyboardMaker maker = new ReplyKeyboardMaker();

        sendMessage.setReplyMarkup(maker.getSingleButtonKeyboard("Войти"));

        return sendMessage;
    }

    private BotApiMethod<?> updateAgentServiceName(Message message, String chatId) {
        AgentServiceMSG service = new AgentServiceMSG();
        if (message.getText().length() > 150) {
            return new SendMessage(chatId, "Максимальное количество символов - 150");
        }
        service.setName(message.getText());
        return updateService(chatId, service);
    }

    private BotApiMethod<?> updateAgentServiceDescription(Message message, String chatId) {
        AgentServiceMSG service = new AgentServiceMSG();
        if (message.getText().length() > 150) {
            return new SendMessage(chatId, "Максимальное количество символов - 150");
        }
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
            if (agentMSG.getName() != null && agentMSG.getName().length() > 20) {
                return new SendMessage(chatId, "Максимальное количество символов - 20");
            }
            if (agentMSG.getPassword() != null && agentMSG.getPassword().length() < 5) {
                return new SendMessage(chatId, "Минимальное количество символов - 5");
            }
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

            if (scheduleMSG.getDateEnd().toEpochDay() - scheduleMSG.getDateStart().toEpochDay() > 30) {
                return new SendMessage(chatId, "Разница между датами должна быть менее 30 дней");
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
                *TapTimes* – сервис онлайн записи клиентов, предназначен для сферы услуг. Обеспечивает ведение клиентской базы и удобную запись для клиентов. Виджет можно установить на сайт или дать клиенту прямую ссылку.

                *Для входа или регистрации нажмите кнопку ниже.*""";
        SendMessage sendMessage = new SendMessage(chatId, text);
        sendMessage.enableMarkdown(true);
        ReplyKeyboardMaker maker = new ReplyKeyboardMaker();

        sendMessage.setReplyMarkup(maker.getSingleButtonKeyboard("Регистрация"));

        return sendMessage;
    }
}
