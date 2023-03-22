package ru.set404.clients.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "Appointments")
@NoArgsConstructor
public class Appointment {
    @Id
    @Column(name = "appointment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(name = "start_time")
    private LocalDateTime startTime;
    @Column(name = "service_id")
    private Long serviceId;
    @Column(name = "therapist_id")
    private Long therapistId;
    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "client_id")
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
