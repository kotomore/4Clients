package ru.set404.telegramservice.models;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentService {
    private String id;
    private String name;
    private String description;
    private double price;
    private int duration;
    private String agentId;
}
