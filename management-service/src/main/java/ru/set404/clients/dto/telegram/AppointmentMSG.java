package ru.set404.clients.dto.telegram;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import ru.set404.clients.dto.ClientDTO;
import ru.set404.clients.models.TimeSlot;

@Getter
@Setter
public class AppointmentMSG {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private TimeSlot time;
    private String serviceName;
    private ClientDTO client;
    private String agentId;
}
