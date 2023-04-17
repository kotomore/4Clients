package ru.set404.clients.models;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class Schedule {
    @Id
    private String id;
    private String agentId;
    private LocalDate date;
    private List<TimeSlot> availableSlots;
}





