package ru.set404.clients.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Appointment {
    private Long appointmentId;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;
    private Long serviceId;
    private Long therapistId;
    private Client client;

    public Appointment(Long appointmentId, LocalDateTime startTime, Long serviceId, Long therapistId, Client client) {
        this.appointmentId = appointmentId;
        this.startTime = startTime;
        this.serviceId = serviceId;
        this.therapistId = therapistId;
        this.client = client;
    }

    public Appointment(LocalDateTime startTime, Long serviceId, Long therapistId, Client client) {
        this.startTime = startTime;
        this.serviceId = serviceId;
        this.therapistId = therapistId;
        this.client = client;
    }
}
