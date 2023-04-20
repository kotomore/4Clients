package ru.set404.authservice.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.set404.clients.models.Agent;
import ru.set404.clients.repositories.AgentRepository;
import ru.set404.clients.services.RegistrationService;

import java.util.Optional;

public class RegistrationServiceTest {
    @Mock
    private AgentRepository agentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ModelMapper modelMapper;

    private RegistrationService registrationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        registrationService = new RegistrationService(agentRepository, passwordEncoder);
    }

    @Test
    public void testSaveAgentWhenAgentDoesNotExist() {
        // Given
        Agent agent = new Agent();
        agent.setPhone("+79123456789");
        agent.setPassword("password");
        agent.setName("Agent");

        Mockito.when(agentRepository.findAgentByPhone(agent.getPhone())).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode(agent.getPassword())).thenReturn("hashedPassword");
        Mockito.when(agentRepository.save(Mockito.any(Agent.class))).thenReturn(agent);

        Agent savedAgent = registrationService.saveAgent(agent);

        Mockito.verify(agentRepository, Mockito.times(1)).findAgentByPhone(savedAgent.getPhone());
        Mockito.verify(agentRepository, Mockito.times(1)).save(Mockito.any(Agent.class));
    }

    @Test
    public void testSaveAgentWhenAgentExists() {
        // Given
        Agent agent = new Agent();
        agent.setPhone("+79123456789");
        agent.setPassword("password");
        agent.setName("Agent");

        Mockito.when(agentRepository.findAgentByPhone(agent.getPhone())).thenReturn(Optional.of(new Agent()));

        Assertions.assertEquals(new Agent().getPhone(), registrationService.saveAgent(agent).getPhone());

        Mockito.verify(agentRepository, Mockito.times(1)).findAgentByPhone(agent.getPhone());
        Mockito.verify(modelMapper, Mockito.never()).map(Mockito.any(Agent.class), Mockito.any(Class.class));
        Mockito.verify(passwordEncoder, Mockito.never()).encode(Mockito.anyString());
        Mockito.verify(agentRepository, Mockito.never()).save(Mockito.any(Agent.class));
    }
}