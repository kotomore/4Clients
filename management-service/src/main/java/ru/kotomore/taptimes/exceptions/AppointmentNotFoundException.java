package ru.kotomore.taptimes.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(String id) {
        super("Записей не обнаружено");
        log.info("Appointments not found for agentId - " + id);
    }
}
