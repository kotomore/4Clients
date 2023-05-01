package ru.set404.clients.models;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document("appointments")
public class Appointment {
    @Id
    private String id;
    private String agentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Client client;
    private String serviceId;
}
