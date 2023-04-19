package ru.set404.clients.exceptions;

public class AgentServiceNotFoundException extends RuntimeException {
    public AgentServiceNotFoundException(String agentId) {
        super("Service with AgentId - "+ agentId + " not found");
    }

}
