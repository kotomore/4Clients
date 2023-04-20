package ru.set404.telegramservice.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
    private String id;
    private LocalDate date;
    private TimeSlot timeSlot;
    private String agentId;
    private Client client;
    private String serviceId;
}
