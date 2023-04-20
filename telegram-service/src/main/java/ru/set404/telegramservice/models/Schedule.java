package ru.set404.telegramservice.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Schedule {
    private String id;
    private String agentId;
    private LocalDate date;
    private List<TimeSlot> availableSlots;
}





