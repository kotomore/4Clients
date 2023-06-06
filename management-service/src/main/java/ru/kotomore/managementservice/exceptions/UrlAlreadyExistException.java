package ru.kotomore.managementservice.exceptions;

public class UrlAlreadyExistException extends RuntimeException {
    public UrlAlreadyExistException() {
        super("Url already exist");
    }

}
