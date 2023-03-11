package ru.set404.clients.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.set404.clients.models.Appointment;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class AppointmentDTO {
    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    @NotNull(message = "Not be empty")
    private LocalDateTime startTime;
    @NotNull(message = "Not be empty")
    private Long serviceId;
    @NotNull(message = "Not be empty")
    private Long therapistId;
    private ClientDTO client;

    public Appointment toAppointment() {
        return new Appointment(startTime, serviceId, therapistId, client.toClient());
    }
}
