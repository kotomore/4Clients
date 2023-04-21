package ru.set404.telegramservice.listeners;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.set404.telegramservice.dto.telegram.AgentMSG;
import ru.set404.telegramservice.dto.telegram.AgentServiceMSG;
import ru.set404.telegramservice.dto.telegram.AppointmentMSG;
import ru.set404.telegramservice.dto.telegram.ScheduleMSG;
import ru.set404.telegramservice.models.TelegramUser;
import ru.set404.telegramservice.repositories.TelegramUserRepository;
import ru.set404.telegramservice.services.MessageService;

import java.util.List;
import java.util.Optional;

@EnableRabbit
@Slf4j
@AllArgsConstructor
@Component
public class RabbitMQListener {
    private TelegramUserRepository repository;
    private MessageService messageService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_agent", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.agent"
    ))
    public void receiveAgent(@Payload AgentMSG agentMSG) {
        log.info("Message to telegram agent info with agent id- " + agentMSG.getName());
        TelegramUser user = repository.findByPhone(agentMSG.getPhone()).orElse(new TelegramUser());
        messageService.sendAgentInfoMessage(user, agentMSG);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_service", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.service"
    ))
    public void receiveService(@Payload AgentServiceMSG service) {
        log.info("Message to telegram service - with agent id " + service.getName());
        TelegramUser user = repository.findByAgentId(service.getAgentId()).orElse(new TelegramUser());
        messageService.sendAgentServiceMessage(user, service);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_bot", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.register"
    ))
    public void registerBot(@Payload AgentMSG agentMSG) {
        log.info("Message to telegram registration with agent id - " + agentMSG.getId());

        Optional<TelegramUser> user = repository.findByPhone(agentMSG.getPhone());
        if (user.isPresent()) {
            user.get().setAgentId(agentMSG.getId());
            repository.save(user.get());
            messageService.sendSuccessRegMessage(user.get());
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_schedule", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.schedule"
    ))
    public void receiveScheduleMSG(@Payload ScheduleMSG scheduleMSG) {
        log.info("Message to telegram schedule for agent - " + scheduleMSG.getAgentId());
        TelegramUser user = repository.findByAgentId(scheduleMSG.getAgentId()).orElse(new TelegramUser());
        messageService.sendAgentSchedule(user, scheduleMSG);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_appointment", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.appointment"
    ))
    public void receiveAppointmentMSG(@Payload List<AppointmentMSG> appointmentMSGS) {
        if (!appointmentMSGS.isEmpty()) {
            log.info("Message to telegram schedule for agent - " + appointmentMSGS.get(0).getAgentId());
            TelegramUser user = repository.findByAgentId(appointmentMSGS.get(0).getAgentId()).orElse(new TelegramUser());
            messageService.sendAgentAppointmentsMessage(user, appointmentMSGS);
        }
    }
}