package ru.set404.telegramservice.dto.telegram;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import ru.set404.telegramservice.dto.ClientDTO;
import ru.set404.telegramservice.models.TimeSlot;

@Getter
@Setter
public class AppointmentMSG {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private TimeSlot time;
    private String serviceName;
    private ClientDTO client;
    private String agentId;
}
