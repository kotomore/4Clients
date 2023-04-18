package ru.set404.clients.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.set404.clients.dto.AgentDTO;
import ru.set404.clients.exceptions.UserAlreadyExistException;
import ru.set404.clients.models.Agent;
import ru.set404.clients.repositories.AgentRepository;

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
        registrationService = new RegistrationService(agentRepository, passwordEncoder, modelMapper);
    }

    @Test
    public void testSaveAgentWhenAgentDoesNotExist() {
        // Given
        AgentDTO agentDTO = new AgentDTO();
        agentDTO.setPhone("+79123456789");
        agentDTO.setPassword("password");
        agentDTO.setName("Agent");

        Mockito.when(agentRepository.findAgentByPhone(agentDTO.getPhone())).thenReturn(Optional.empty());
        Mockito.when(modelMapper.map(agentDTO, Agent.class)).thenReturn(new Agent());
        Mockito.when(passwordEncoder.encode(agentDTO.getPassword())).thenReturn("hashedPassword");
        Mockito.when(agentRepository.save(Mockito.any(Agent.class))).thenReturn(new Agent());

        registrationService.saveAgent(agentDTO);

        Mockito.verify(agentRepository, Mockito.times(1)).findAgentByPhone(agentDTO.getPhone());
        Mockito.verify(modelMapper, Mockito.times(1)).map(agentDTO, Agent.class);
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(agentDTO.getPassword());
        Mockito.verify(agentRepository, Mockito.times(1)).save(Mockito.any(Agent.class));
    }

    @Test
    public void testSaveAgentWhenAgentExists() {
        // Given
        AgentDTO agentDTO = new AgentDTO();
        agentDTO.setPhone("+79123456789");
        agentDTO.setPassword("password");
        agentDTO.setName("Agent");

        Mockito.when(agentRepository.findAgentByPhone(agentDTO.getPhone())).thenReturn(Optional.of(new Agent()));

        Assertions.assertThrows(UserAlreadyExistException.class, () -> registrationService.saveAgent(agentDTO));

        Mockito.verify(agentRepository, Mockito.times(1)).findAgentByPhone(agentDTO.getPhone());
        Mockito.verify(modelMapper, Mockito.never()).map(Mockito.any(AgentDTO.class), Mockito.any(Class.class));
        Mockito.verify(passwordEncoder, Mockito.never()).encode(Mockito.anyString());
        Mockito.verify(agentRepository, Mockito.never()).save(Mockito.any(Agent.class));
    }
}