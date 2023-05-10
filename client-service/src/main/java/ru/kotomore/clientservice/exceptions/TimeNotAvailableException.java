package ru.kotomore.clientservice.exceptions;

public class TimeNotAvailableException extends RuntimeException {
    public TimeNotAvailableException() {
        super("Time is not available");
    }

}
