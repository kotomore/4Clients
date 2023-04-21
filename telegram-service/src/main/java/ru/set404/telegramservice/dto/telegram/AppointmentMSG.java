package ru.set404.telegramservice.dto.telegram;

import lombok.Getter;
import lombok.Setter;
import ru.set404.telegramservice.dto.ClientDTO;

@Getter
@Setter
public class AppointmentMSG {
    private String date;
    private String startTime;
    private String endTime;
    private String serviceName;
    private ClientDTO client;
    private String agentId;
}
