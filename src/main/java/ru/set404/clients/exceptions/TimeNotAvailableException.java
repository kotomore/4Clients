package ru.set404.clients.exceptions;

public class TimeNotAvailableException extends RuntimeException{
    public TimeNotAvailableException() {
        super("Time is not available");
    }

}
