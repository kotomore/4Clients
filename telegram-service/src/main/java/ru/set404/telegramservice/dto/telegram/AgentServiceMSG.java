package ru.set404.telegramservice.dto.telegram;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AgentServiceMSG implements Serializable {
    private String id;
    private String name;
    private String description;
    private double price;
    private int duration;
    private String agentId;
}
