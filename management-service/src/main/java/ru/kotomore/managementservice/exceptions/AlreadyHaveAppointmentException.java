package ru.kotomore.managementservice.exceptions;

public class AlreadyHaveAppointmentException extends RuntimeException {
    public AlreadyHaveAppointmentException() {
        super("Already have appointment on this time");
    }

}
