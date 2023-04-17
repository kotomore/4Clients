package ru.set404.clients.exceptions;

public class AgentNotFoundException extends RuntimeException {
    public AgentNotFoundException(String id) {
        super("Could not find therapist for id - " + id);
    }

}
