package ru.set404.clients.models;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Schedule {
    @Id
    private String id;
    private String agentId;
    private LocalDate date;
    private LocalTime workTimeStart;
    private LocalTime workTimeEnd;
    private List<TimeSlot> availableSlots;

    public Schedule(String id, String agentId, LocalDate date, List<TimeSlot> availableSlots) {
        this.id = id;
        this.agentId = agentId;
        this.date = date;
        this.availableSlots = availableSlots;
    }
}





