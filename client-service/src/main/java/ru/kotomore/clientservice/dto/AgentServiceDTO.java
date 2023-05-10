package ru.kotomore.clientservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentServiceDTO {
    //Service side
    private String id;
    private String name;
    private String description;
    private double price;
    private int duration;

    //Agent side
    private String agentName;
    private String agentPhone;
}
