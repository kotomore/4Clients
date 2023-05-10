package ru.kotomore.telegramservice.messaging;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.kotomore.telegramservice.services.TelegramMessageService;
import telegram.*;

import java.util.List;

@EnableRabbit
@Slf4j
@AllArgsConstructor
@Component
public class RabbitListener {
    private TelegramMessageService telegramMessageService;

    @org.springframework.amqp.rabbit.annotation.RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_agent", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.agent"
    ))
    public void receiveAgent(@Payload AgentMSG agentMSG) {
        log.info("Message to telegram agent info with agent id- " + agentMSG.getName());
        telegramMessageService.sendAgentInfoMessage(agentMSG);
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_service", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.service"
    ))
    public void receiveService(@Payload AgentServiceMSG service) {
        log.info("Message to telegram service - with agent id " + service.getName());
        telegramMessageService.sendAgentServiceMessage(service);
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_bot", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.register"
    ))
    public void registerBot(@Payload AgentMSG agentMSG) {
        log.info("Message to telegram registration with agent id - " + agentMSG.getId());
        telegramMessageService.registerUser(agentMSG);
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_schedule", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.schedule"
    ))
    public void receiveScheduleMSG(@Payload AvailabilityMSG availabilityMSG) {
        log.info("Message to telegram schedule for agent - " + availabilityMSG.getAgentId());
        telegramMessageService.sendAgentSchedule(availabilityMSG);
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_appointment", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.appointment"
    ))
    public void receiveAppointmentMSG(@Payload AppointmentMSG appointmentMSG) {
        log.info("Message to telegram schedule for agent - " + appointmentMSG.getAgentId());
        telegramMessageService.sendAgentAppointmentsMessage(List.of(appointmentMSG));
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_all_appointment", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.all_appointment"
    ))
    public void receiveAllAppointmentMSG(@Payload List<AppointmentMSG> appointmentMSGS) {
        log.info("Message to telegram schedule for agent - " + appointmentMSGS.get(0).getAgentId());
        telegramMessageService.sendAgentAppointmentsMessage(appointmentMSGS);
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_error", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.error"
    ))
    public void receiveErrorMSG(@Payload ErrorMSG errorMSG) {
        log.info("Message to telegram schedule for agent - " + errorMSG.getAgentId());
        telegramMessageService.sendErrorMessage(errorMSG);
    }
}