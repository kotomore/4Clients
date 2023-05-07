package ru.kotomore.taptimes.exceptions;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(String id) {
        super("Could not find client " + id);
    }

}
