package ru.kotomore.clientservice.exceptions;

public class AgentNotFoundException extends RuntimeException {
    public AgentNotFoundException(String agentId) {
        super("Agent with id - "+ agentId + " not found");
    }

}
