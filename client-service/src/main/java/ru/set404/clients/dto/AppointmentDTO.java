package ru.set404.clients.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.set404.clients.models.Appointment;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentDTO {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @NotNull(message = "Not be empty")
    private LocalDateTime startTime;
    @NotNull(message = "Not be empty")
    private String agentId;
    @NotNull(message = "Not be empty")
    private String serviceId;
    private ClientDTO client;
}
