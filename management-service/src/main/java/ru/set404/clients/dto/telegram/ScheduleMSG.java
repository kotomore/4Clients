package ru.set404.clients.dto.telegram;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleMSG {
    private String dateStart;
    private String dateEnd;
    private String timeStart;
    private String timeEnd;
    private String serviceId;
    private String agentId;
}
