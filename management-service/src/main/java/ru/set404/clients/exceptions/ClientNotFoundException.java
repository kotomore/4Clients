package ru.set404.clients.exceptions;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(String id) {
        super("Could not find client " + id);
    }

}
