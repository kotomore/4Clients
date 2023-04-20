package ru.set404.telegramservice.dto;

import ru.set404.telegramservice.models.TimeSlot;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentDTO {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private TimeSlot time;
    private String serviceName;
    private ClientDTO client;
}
