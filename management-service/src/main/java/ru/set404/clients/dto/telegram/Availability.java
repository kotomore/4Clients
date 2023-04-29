package ru.set404.clients.dto.telegram;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class Availability {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
