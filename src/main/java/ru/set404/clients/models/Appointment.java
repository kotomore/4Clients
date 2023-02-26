package ru.set404.clients.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

public class Appointment {
    private Long appointmentId;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private Timestamp startTime;
    private Long serviceId;
    private Long therapistId;
    private Client client;

    public Appointment() {
    }

    public Appointment(Long appointmentId, Timestamp startTime, Long serviceId, Long therapistId, Client client) {
        this.appointmentId = appointmentId;
        this.startTime = startTime;
        this.serviceId = serviceId;
        this.therapistId = therapistId;
        this.client = client;
    }

    public Appointment(Timestamp startTime, Long serviceId, Long therapistId, Client client) {
        this.startTime = startTime;
        this.serviceId = serviceId;
        this.therapistId = therapistId;
        this.client = client;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
