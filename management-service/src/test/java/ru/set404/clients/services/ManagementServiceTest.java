package ru.set404.clients.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.set404.clients.dto.AgentDTO;
import ru.set404.clients.exceptions.AgentNotFoundException;
import ru.set404.clients.exceptions.AppointmentNotFoundException;
import ru.set404.clients.exceptions.TimeNotAvailableException;
import ru.set404.clients.models.*;
import ru.set404.clients.repositories.AgentRepository;
import ru.set404.clients.repositories.AppointmentRepository;
import ru.set404.clients.repositories.AvailabilityRepository;

import java.time.LocalDate;
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
    void findAllAppointments() {
        Mockito.when(appointmentRepository.findByAgentId("agent-123")).thenReturn(List.of(new Appointment()));

        List<Appointment> appointments = managementService.findAllAppointments("agent-123");
        Assertions.assertNotNull(appointments);
    }

    @Test
    void findAllAppointmentsThrowsException() {
        Mockito.when(appointmentRepository.findByAgentId("agent-123")).thenReturn(List.of());

        Assertions.assertThrows(AppointmentNotFoundException.class,
                () -> managementService.findAllAppointments("agent-123"));
    }

    @Test
    void findAppointmentById() {
        Appointment appointment = new Appointment();
        appointment.setServiceId("appointment-123");
        appointment.setAgentId("agent-123");
        appointment.setClient(new Client());
        appointment.setTimeSlot(new TimeSlot());

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
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(LocalTime.of(22, 0));
        timeSlot.setEndTime(LocalTime.of(23, 0));

        Appointment appointment = new Appointment();
        appointment.setServiceId("appointment-123");
        appointment.setAgentId("agent-123");
        appointment.setClient(new Client());
        appointment.setTimeSlot(timeSlot);

        //Add availability
        Availability availability = new Availability();
        availability.setDate(LocalDate.now());
        availability.setStartTime(LocalTime.of(22, 0));
        availability.setEndTime(LocalTime.of(23, 0));
        availability.setAgentId("agent-123");



        Mockito.when(appointmentRepository.findByIdAndAgentId("appointment-123", "agent-123"))
                .thenReturn(Optional.of(appointment));
        Mockito.when(availabilityRepository.findByAgentIdAndDateAndStartTime(
                appointment.getAgentId(),
                appointment.getDate(),
                appointment.getTimeSlot().getStartTime())).thenReturn(Optional.of(availability));


        managementService.deleteAppointment("agent-123", "appointment-123");

        Mockito.verify(appointmentRepository).deleteByIdAndAgentId("appointment-123", "agent-123");
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
        Availability availability = new Availability();
        availability.setDate(LocalDate.now());
        availability.setStartTime(LocalTime.of(22, 0));
        availability.setEndTime(LocalTime.of(23, 0));
        availability.setAgentId("agent-123");

        Mockito.when(availabilityRepository.findByAgentIdAndDate("agent-123", LocalDate.now())).thenReturn(List.of(availability));
        Set<LocalTime> times = managementService.findAvailableTimes("agent-123", LocalDate.now());
        Assertions.assertEquals(1, times.size());
    }

    @Test
    void findAvailableTimesThrowsException() {
        Mockito.when(availabilityRepository.findByAgentIdAndDate("agent-123", LocalDate.now())).thenReturn(List.of());

        Assertions.assertThrows(TimeNotAvailableException.class,
                () -> managementService.findAvailableTimes("agent-123", LocalDate.now()));
    }
}
