package ru.set404.clients.dto.telegram;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Availability {
    private String date;
    private String startTime;
    private String endTime;
}
