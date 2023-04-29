package ru.set404.clients.dto.telegram;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AvailabilityMSG {
    private String agentId;
    private List<Availability> availabilities;
}
