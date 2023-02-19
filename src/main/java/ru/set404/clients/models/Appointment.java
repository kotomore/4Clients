package ru.set404.clients.models;

import java.sql.Timestamp;

public class Appointment {
    private Long appointmentId;
    private Timestamp startTime;
    private Timestamp endTime;
    private Long serviceId;
    private Long therapistId;
    private Client client;

    public Appointment() {
    }

    public Appointment(Long appointmentId, Timestamp startTime, Timestamp endTime, Long serviceId, Long therapistId, Client client) {
        this.appointmentId = appointmentId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.serviceId = serviceId;
        this.therapistId = therapistId;
        this.client = client;
    }

    public Appointment(Timestamp startTime, Timestamp endTime, Long serviceId, Long therapistId, Client client) {
        this.startTime = startTime;
        this.endTime = endTime;
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

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
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
