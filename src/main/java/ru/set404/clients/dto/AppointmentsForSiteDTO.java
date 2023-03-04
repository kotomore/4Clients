package ru.set404.clients.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class AppointmentsForSiteDTO {
    private Long id;
    private String title;
    private String category;
    private LocalDateTime start;
    private LocalDateTime end;
}
