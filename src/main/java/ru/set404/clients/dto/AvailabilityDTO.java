package ru.set404.clients.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@Getter
@Setter
public class AvailabilityDTO {
    @NotNull(message = "Not be empty")
    private LocalDate date;
    @NotNull(message = "Not be empty")
    private LocalTime startTime;
    @NotNull(message = "Not be empty")
    private LocalTime endTime;
}
