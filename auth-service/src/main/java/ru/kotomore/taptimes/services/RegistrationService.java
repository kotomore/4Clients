package ru.kotomore.taptimes.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.kotomore.taptimes.repositories.AgentRepository;
import ru.kotomore.taptimes.models.Agent;
import ru.kotomore.taptimes.models.Role;

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
