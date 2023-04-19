package ru.set404.clients.models;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document("appointments")
public class Appointment {
    @Id
    private String id;
    private LocalDate date;
    private TimeSlot timeSlot;
    private String agentId;
    private Client client;
    private String serviceId;
}
