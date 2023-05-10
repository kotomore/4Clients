package ru.kotomore.managementservice.models;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Availability {
    @Id
    private String id;
    private String agentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
