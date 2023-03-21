package ru.set404.clients.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AppointmentsForSiteDTO {
    @Id
    @NotNull(message = "Not be empty")
    private Long id;
    @NotEmpty(message = "Not be empty")
    private String title;
    @NotEmpty(message = "Not be empty")
    private String category;
    @NotNull(message = "Not be empty")
    private LocalDateTime start;
    @NotNull(message = "Not be empty")
    private LocalDateTime end;

    public AppointmentsForSiteDTO(Long id, String title, String category, LocalDateTime timeStart, int duration){
        this.id = id;
        this.title = title;
        this.category = category;
        this.start = timeStart;
        this.end = timeStart.plusMinutes(duration);
    }
}
