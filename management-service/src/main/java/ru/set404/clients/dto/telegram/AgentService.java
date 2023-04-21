package ru.set404.clients.dto.telegram;

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
