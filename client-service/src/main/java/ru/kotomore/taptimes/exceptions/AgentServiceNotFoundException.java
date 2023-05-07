package ru.kotomore.taptimes.exceptions;

public class AgentServiceNotFoundException extends RuntimeException {
    public AgentServiceNotFoundException(String agentId) {
        super("Service with AgentId - "+ agentId + " not found");
    }

}
