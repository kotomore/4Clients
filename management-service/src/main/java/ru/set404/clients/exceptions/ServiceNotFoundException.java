package ru.set404.clients.exceptions;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(String id) {
        super("Could not find service for agent id - " + id);
    }

}
