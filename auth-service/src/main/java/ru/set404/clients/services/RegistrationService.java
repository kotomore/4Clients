package ru.set404.clients.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.set404.clients.models.Agent;
import ru.set404.clients.models.Role;
import ru.set404.clients.repositories.AgentRepository;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    public Agent saveAgent(Agent agent) {
        if (agentRepository.findAgentByPhone(agent.getPhone()).isPresent())
            return new Agent();
        agent.setPassword(passwordEncoder.encode(agent.getPassword()));
        agent.setRole(Role.USER);
        return agentRepository.save(agent);
    }
}
