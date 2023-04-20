package ru.set404.clients.listeners;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.set404.clients.dto.AgentDTO;
import ru.set404.clients.dto.TelegramMessage;
import ru.set404.clients.models.Agent;
import ru.set404.clients.models.AgentService;
import ru.set404.clients.services.ManagementService;

@EnableRabbit
@Slf4j
@AllArgsConstructor
@Component
public class RabbitMQListener {

    private ManagementService managementService;
    private AmqpTemplate template;
    private TopicExchange telegramExchange;

    @RabbitListener(queues = "telegram_queue_from", returnExceptions = "false")
    public void receive(TelegramMessage message) {
        log.info("Message from telegram - " + message.getAction());
        switch (message.getAction()) {
            case AGENT_INFO -> {
                AgentDTO agentDTO = managementService.findAgentDTOById(message.getAgentId());
                template.convertAndSend(telegramExchange.getName(), "telegram_key.agent", agentDTO);
            }

            case SERVICE_INFO -> {
                AgentService service = managementService.findService(message.getAgentId());
                template.convertAndSend(telegramExchange.getName(), "telegram_key.service", service);
            }

            case REGISTER_BOT -> {
                Agent agent = managementService.findOrCreateAgentByPhone(message.getAgentId());
                template.convertAndSend(telegramExchange.getName(), "telegram_key.register", agent);
            }
        }
    }
}