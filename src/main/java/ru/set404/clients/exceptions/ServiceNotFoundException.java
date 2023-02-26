package ru.set404.clients.exceptions;

public class ServiceNotFoundException extends RuntimeException{
    public ServiceNotFoundException(Long id) {
        super("Could not find service for therapist id - " + id);
    }

}
