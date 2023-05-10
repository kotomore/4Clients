package ru.kotomore.managementservice.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kotomore.managementservice.dto.AgentDTO;
import ru.kotomore.managementservice.exceptions.AgentNotFoundException;
import ru.kotomore.managementservice.exceptions.AppointmentNotFoundException;
import ru.kotomore.managementservice.exceptions.TimeNotAvailableException;
import ru.kotomore.managementservice.models.Agent;
import ru.kotomore.managementservice.models.Appointment;
import ru.kotomore.managementservice.models.Availability;
import ru.kotomore.managementservice.models.Client;
import ru.kotomore.managementservice.repositories.AgentRepository;
import ru.kotomore.managementservice.repositories.AppointmentRepository;
import ru.kotomore.managementservice.repositories.AvailabilityRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class ManagementServiceTest {
    @InjectMocks
    private ManagementService managementService;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void findAllAppointmentsThrowsException() {
        Assertions.assertThrows(AppointmentNotFoundException.class,
                () -> managementService.findAllAppointments("agent-123"));
    }

    @Test
    void findAppointmentById() {
        Appointment appointment = new Appointment();
        appointment.setServiceId("appointment-123");
        appointment.setAgentId("agent-123");
        appointment.setClient(new Client());

        Mockito.when(appointmentRepository.findByIdAndAgentId("appointment-123", "agent-123"))
                .thenReturn(Optional.of(appointment));
        Appointment createdAppointment = managementService.findAppointmentById("agent-123", "appointment-123");

        Assertions.assertNotNull(appointment);
        Assertions.assertEquals(appointment, createdAppointment);
    }

    @Test
    void findAppointmentByIdThrowsException() {
        Mockito.when(appointmentRepository.findByIdAndAgentId("appointment-123", "agent-123"))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(AppointmentNotFoundException.class,
                () -> managementService.findAppointmentById("agent-123", "appointment-123"));
    }

    @Test
    void deleteAppointment() {

        //Add appointment
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 0));
        LocalDateTime endTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 0));

        Appointment appointment = new Appointment();
        appointment.setServiceId("appointment-123");
        appointment.setAgentId("agent-123");
        appointment.setClient(new Client());
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);

        //Add availability
        Availability availability = new Availability();
        availability.setStartTime(appointment.getStartTime());
        availability.setEndTime(appointment.getEndTime());
        availability.setAgentId("agent-123");

        Mockito.when(appointmentRepository.findByIdAndAgentId("appointment-123", "agent-123"))
                .thenReturn(Optional.of(appointment));

        managementService.deleteAppointment("agent-123", "appointment-123");

        Mockito.verify(appointmentRepository).delete(appointment);
    }

    @Test
    void deleteAppointmentThrowsException() {
        Mockito.when(appointmentRepository.findByIdAndAgentId("appointment-123", "agent-123"))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(AppointmentNotFoundException.class,
                () -> managementService.deleteAppointment("agent-123", "appointment-123"));
    }

    @Test
    void findAgentById() {
        Agent agent = new Agent();
        agent.setPhone("123-456-7890");
        agent.setName("John");
        agent.setPassword("password");
        Mockito.when(agentRepository.findById("agent-123")).thenReturn(Optional.of(agent));
        Agent updatedAgent = managementService.findAgentById("agent-123");
        Assertions.assertNotNull(agent);
        Assertions.assertEquals(agent, updatedAgent);
    }

    @Test
    void findAgentByIdThrowsException() {
        Mockito.when(agentRepository.findById("agent-123")).thenReturn(Optional.empty());

        Assertions.assertThrows(AgentNotFoundException.class,
                () -> managementService.findAgentById("agent-123"));
    }

    @Test
    void updateAgent() {
        Agent agent = new Agent();
        agent.setPhone("123-456-7890");
        agent.setName("John");
        agent.setPassword("password");


        Mockito.when(agentRepository.findById("agent-123")).thenReturn(Optional.of(agent));
        Mockito.when(passwordEncoder.encode("password")).thenReturn("encoded password");

        managementService.updateAgent("agent-123", new AgentDTO("Agent updated name", "123", "password"));
        Mockito.verify(agentRepository).save(agent);
    }

    @Test
    void updateAgentThrowsException() {
        Mockito.when(agentRepository.findById("agent-123")).thenReturn(Optional.empty());

        Assertions.assertThrows(AgentNotFoundException.class,
                () -> managementService.updateAgent("agent-123", new AgentDTO()));
    }

    @Test
    void findAvailableTimes() {
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 0));
        LocalDateTime endTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 0));

        Availability availability = new Availability();
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        availability.setAgentId("agent-123");

        Mockito.when(availabilityRepository.findByAgentIdAndStartTimeBetween("agent-123",
                LocalDateTime.of(LocalDate.now(), LocalTime.MIN), LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN))).thenReturn(List.of(availability));
        Set<LocalTime> times = managementService.findAvailableTimes("agent-123", LocalDate.now());
        Assertions.assertEquals(1, times.size());
    }

    @Test
    void findAvailableTimesThrowsException() {
        Mockito.when(availabilityRepository.findByAgentIdAndStartTimeBetween("agent-123",
                LocalDateTime.of(LocalDate.now(), LocalTime.MIN), LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN))).thenReturn(List.of());

        Assertions.assertThrows(TimeNotAvailableException.class,
                () -> managementService.findAvailableTimes("agent-123", LocalDate.now()));
    }
}
