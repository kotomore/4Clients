package ru.set404.clients.exceptions;

public class AppointmentNotFoundException extends RuntimeException{
    public AppointmentNotFoundException(Long id) {
        super("Could not find appointments for id - " + id);
    }

}
