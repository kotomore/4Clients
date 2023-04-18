package ru.set404.clients.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import ru.set404.clients.dto.AppointmentDTO;
import ru.set404.clients.dto.ClientDTO;
import ru.set404.clients.models.AgentService;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.repositories.AppointmentRepository;
import ru.set404.clients.repositories.ScheduleRepository;
import ru.set404.clients.repositories.ServiceRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClientServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clientService = new ClientService(scheduleRepository, appointmentRepository, serviceRepository, modelMapper);
    }

    @Test
    void createAppointment_validInput_appointmentSaved() {
        // Arrange
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setAgentId("agent-1");
        appointmentDTO.setServiceId("service-1");
        appointmentDTO.setStartTime(LocalDateTime.of(2023, 4, 19, 9, 0));
        appointmentDTO.setClient(new ClientDTO());

        AgentService service = new AgentService();
        service.setId("service-1");
        service.setDuration(60);

        when(serviceRepository.findById(eq("service-1"))).thenReturn(Optional.of(service));

        clientService.createAppointment(appointmentDTO);

        verify(appointmentRepository).save(any(Appointment.class));
    }
}