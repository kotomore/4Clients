package ru.set404.clients.exceptions;

public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(String id) {
        super("Could not find appointments for id - " + id);
    }

}
