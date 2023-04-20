package ru.set404.telegramservice.dto;

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
        SERVICE_INFO
    }
}

