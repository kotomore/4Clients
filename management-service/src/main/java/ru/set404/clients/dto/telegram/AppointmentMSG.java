package ru.set404.clients.dto.telegram;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentMSG {
    private String date;
    private String startTime;
    private String endTime;
    private String serviceName;
    private String clientName;
    private String clientPhone;
    private String agentId;
}
