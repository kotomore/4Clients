package ru.set404.clients.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class TimeSlotDTO {
    private LocalDate dateStart;
    private LocalDate dateEnd;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private String serviceId;
}
