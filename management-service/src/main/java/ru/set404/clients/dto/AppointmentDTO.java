package ru.set404.clients.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.set404.clients.models.TimeSlot;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentDTO {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private TimeSlot time;
    private String serviceName;
    private ClientDTO client;
}
