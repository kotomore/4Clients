package ru.set404.clients.services;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.set404.clients.dto.AgentDTO;
import ru.set404.clients.exceptions.UserAlreadyExistException;
import ru.set404.clients.models.Agent;
import ru.set404.clients.models.Role;
import ru.set404.clients.repositories.AgentRepository;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public void saveAgent(AgentDTO agentDTO) {
        if (agentRepository.findAgentByPhone(agentDTO.getPhone()).isPresent())
            throw new UserAlreadyExistException();
        Agent agent = modelMapper.map(agentDTO, Agent.class);
        agent.setPassword(passwordEncoder.encode(agentDTO.getPassword()));
        agent.setRole(Role.USER);
        agentRepository.save(agent);
    }
}
