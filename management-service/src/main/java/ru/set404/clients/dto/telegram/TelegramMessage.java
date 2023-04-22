package ru.set404.clients.dto.telegram;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelegramMessage {
    private String agentId;
    private Action action;

    public enum Action {
        REGISTER_BOT,
        AGENT_INFO,
        SERVICE_INFO,
        SCHEDULES,
        APPOINTMENTS
    }
}

