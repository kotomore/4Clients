package ru.set404.clients.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.set404.clients.models.Appointment;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AppointmentDTO {
    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;
    private Long serviceId;
    private Long therapistId;
    private ClientDTO client;

    public AppointmentDTO() {
    }

    public Appointment toAppointment() {
        return new Appointment(startTime, serviceId, therapistId, client.toClient());
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(Long therapistId) {
        this.therapistId = therapistId;
    }

    public ClientDTO getClient() {
        return client;
    }

    public void setClient(ClientDTO client) {
        this.client = client;
    }
}
