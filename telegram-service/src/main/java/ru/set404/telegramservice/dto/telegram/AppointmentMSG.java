package ru.set404.telegramservice.dto.telegram;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AppointmentMSG implements Serializable {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String serviceName;
    private String clientName;
    private String clientPhone;
    private String agentId;
    private Type type;

    public enum Type {OLD, NEW}
}
