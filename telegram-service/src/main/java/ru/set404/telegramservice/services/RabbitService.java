package ru.set404.telegramservice.services;

import ru.set404.telegramservice.dto.TelegramMessage;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RabbitService {
    private final AmqpTemplate template;

    public void registerAgentByPhone(String phone) {
        TelegramMessage message = new TelegramMessage();
        message.setAgentId(phone);
        message.setAction(TelegramMessage.Action.REGISTER_BOT);
        template.convertAndSend("telegram_queue_from", message);
    }

    public void getAgentInfo(String agentId) {
        TelegramMessage message = new TelegramMessage();
        message.setAgentId(agentId);
        message.setAction(TelegramMessage.Action.AGENT_INFO);
        template.convertAndSend("telegram_queue_from", message);
    }

    public void getServiceInfo(String agentId) {
        TelegramMessage message = new TelegramMessage();
        message.setAgentId(agentId);
        message.setAction(TelegramMessage.Action.SERVICE_INFO);
        template.convertAndSend("telegram_queue_from", message);
    }
}
