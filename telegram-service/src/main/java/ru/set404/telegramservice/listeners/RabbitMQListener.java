package ru.set404.telegramservice.listeners;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import telegram.*;
import ru.set404.telegramservice.models.TelegramUser;
import ru.set404.telegramservice.repositories.TelegramUserRepository;
import ru.set404.telegramservice.services.TelegramMessageService;

import java.util.Optional;

@EnableRabbit
@Slf4j
@AllArgsConstructor
@Component
public class RabbitMQListener {
    private TelegramUserRepository repository;
    private TelegramMessageService telegramMessageService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_agent", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.agent"
    ))
    public void receiveAgent(@Payload AgentMSG agentMSG) {
        log.info("Message to telegram agent info with agent id- " + agentMSG.getName());
        TelegramUser user = repository.findByPhone(agentMSG.getPhone()).orElse(new TelegramUser());
        telegramMessageService.sendAgentInfoMessage(user, agentMSG);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_service", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.service"
    ))
    public void receiveService(@Payload AgentServiceMSG service) {
        log.info("Message to telegram service - with agent id " + service.getName());
        TelegramUser user = repository.findByAgentId(service.getAgentId()).orElse(new TelegramUser());
        telegramMessageService.sendAgentServiceMessage(user, service);
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
            telegramMessageService.sendSuccessRegMessage(user.get());
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_schedule", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.schedule"
    ))
    public void receiveScheduleMSG(@Payload AvailabilityMSG availabilityMSG) {
        log.info("Message to telegram schedule for agent - " + availabilityMSG.getAgentId());
        TelegramUser user = repository.findByAgentId(availabilityMSG.getAgentId()).orElse(new TelegramUser());
        telegramMessageService.sendAgentSchedule(user, availabilityMSG);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_appointment", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.appointment"
    ))
    public void receiveAppointmentMSG(@Payload AppointmentMSG appointmentMSG) {
        log.info("Message to telegram schedule for agent - " + appointmentMSG.getAgentId());
        TelegramUser user = repository.findByAgentId(appointmentMSG.getAgentId()).orElse(new TelegramUser());
        telegramMessageService.sendAgentAppointmentsMessage(user, appointmentMSG);
    }
}