package ru.kotomore.clientservice.exceptions;

public class TelegramServiceNotAvailableException extends RuntimeException {
    public TelegramServiceNotAvailableException() {
        super("Can't send telegram message");
    }
}
