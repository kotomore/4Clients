package ru.set404.telegramservice.services;

import org.springframework.amqp.core.Queue;
import ru.set404.telegramservice.dto.telegram.ScheduleMSG;
import ru.set404.telegramservice.dto.telegram.TelegramMessage;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import ru.set404.telegramservice.dto.telegram.AgentMSG;
import ru.set404.telegramservice.dto.telegram.AgentServiceMSG;

@Component
@AllArgsConstructor
public class RabbitService {
    private final AmqpTemplate template;
    private final Queue telegramQueueFrom;

    public void registerAgentByPhone(String phone) {
        TelegramMessage message = new TelegramMessage();
        message.setAgentId(phone);
        message.setAction(TelegramMessage.Action.REGISTER_BOT);
        template.convertAndSend(telegramQueueFrom.getName(), message);
    }

    public void updateService(AgentServiceMSG service) {
        template.convertAndSend("telegram_update_service", service);
    }

    public void updateAgent(AgentMSG agentMSG) {
        template.convertAndSend("telegram_update_agent", agentMSG);
    }

    public void updateSchedule(ScheduleMSG scheduleMSG) {
        try {
            template.convertAndSend("telegram_update_schedule", scheduleMSG);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendTelegramMessage(String agentId, TelegramMessage.Action action) {
        TelegramMessage message = new TelegramMessage();
        message.setAgentId(agentId);
        message.setAction(action);
        template.convertAndSend(telegramQueueFrom.getName(), message);
    }
}
