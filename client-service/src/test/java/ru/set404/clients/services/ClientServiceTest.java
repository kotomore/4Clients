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
import ru.set404.clients.models.Schedule;
import ru.set404.clients.models.TimeSlot;
import ru.set404.clients.repositories.AgentRepository;
import ru.set404.clients.repositories.AppointmentRepository;
import ru.set404.clients.repositories.ScheduleRepository;
import ru.set404.clients.repositories.ServiceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private AgentRepository agentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clientService = new ClientService(scheduleRepository, appointmentRepository, serviceRepository, agentRepository, modelMapper);
    }

    @Test
    void createAppointment_validInput_appointmentSaved() {
        // Arrange
        String agentId = "agent-1";
        LocalDate date = LocalDate.now();


        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setAgentId(agentId);
        appointmentDTO.setServiceId("service-1");
        appointmentDTO.setStartTime(date.atTime(10, 0));
        appointmentDTO.setClient(new ClientDTO());

        AgentService service = new AgentService();
        service.setId("service-1");
        service.setDuration(60);

        TimeSlot timeSlot = new TimeSlot(LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot timeSlot2 = new TimeSlot(LocalTime.of(11, 0), LocalTime.of(12, 0));

        List<TimeSlot> timeSlots = new ArrayList<>();
        timeSlots.add(timeSlot);
        timeSlots.add(timeSlot2);

        Schedule schedule = new Schedule();
        schedule.setDate(date);
        schedule.setAgentId(agentId);
        schedule.setAvailableSlots(timeSlots);

        when(serviceRepository.findById(eq("service-1"))).thenReturn(Optional.of(service));
        when(scheduleRepository.findByAgentIdAndDate(agentId, date)).thenReturn(Optional.of(schedule));

        clientService.createAppointment(appointmentDTO);

        verify(appointmentRepository).save(any(Appointment.class));
    }
}