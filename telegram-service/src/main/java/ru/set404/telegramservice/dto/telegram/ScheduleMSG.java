package ru.set404.telegramservice.dto.telegram;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ScheduleMSG {
    private String dateStart;
    private String dateEnd;
    private String timeStart;
    private String timeEnd;
    private String serviceId;
    private String agentId;
}
