package ru.kotomore.managementservice.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(String id) {
        super("Не заполнена информация об услуге");
        log.info("Could not find service for agent id - " + id);
    }
}
