package ru.set404.telegramservice.listeners;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.set404.telegramservice.dto.AgentDTO;
import ru.set404.telegramservice.models.Agent;
import ru.set404.telegramservice.models.AgentService;
import ru.set404.telegramservice.models.TelegramUser;
import ru.set404.telegramservice.repositories.TelegramUserRepository;
import ru.set404.telegramservice.services.MessageService;

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
    public void receiveAgent(@Payload AgentDTO agentDTO) {
        log.info("Message to telegram agent - " + agentDTO.getName());
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_service", durable = "true"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.service"
    ))
    public void receiveService(@Payload AgentService service) {
        log.info("Message to telegram service - " + service.getName());
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "telegram_queue_to_bot", durable = "false"),
            exchange = @Exchange(value = "telegram_exchange", type = ExchangeTypes.TOPIC),
            key = "telegram_key.register"
    ))
    public void registerBot(@Payload Agent agent) {
        log.info("Message to telegram bot - " + agent.getId());

        Optional<TelegramUser> user = repository.findByPhone(agent.getPhone());
        if (user.isPresent()) {
            user.get().setAgentId(agent.getId());
            repository.save(user.get());
            messageService.sendSuccessRegMessage(user.get());
        }
    }
}