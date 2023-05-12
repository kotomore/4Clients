package ru.kotomore.clientservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.kotomore.clientservice.dto.AppointmentDTO;
import ru.kotomore.clientservice.exceptions.TimeNotAvailableException;
import ru.kotomore.clientservice.messaging.RabbitMessageSender;
import ru.kotomore.clientservice.models.AgentService;
import ru.kotomore.clientservice.models.Appointment;
import ru.kotomore.clientservice.models.Availability;
import ru.kotomore.clientservice.repositories.AppointmentRepository;
import ru.kotomore.clientservice.repositories.AvailabilityRepository;
import ru.kotomore.clientservice.repositories.ServiceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private AvailabilityRepository availabilityRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private RabbitMessageSender rabbitMessageSender;

    @InjectMocks
    private ClientService clientService;

    @Test
    public void shouldCreateAppointment() {
        // given
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setAgentId("agent1");
        appointmentDTO.setServiceId("1");
        appointmentDTO.setStartTime(LocalDateTime.of(2020, 1, 1, 10, 0));

        AgentService agentService = new AgentService();
        agentService.setDuration(30);
        when(serviceRepository.findById("1")).thenReturn(Optional.of(agentService));
        when(availabilityRepository.existsByAgentIdAndStartTime("agent1", LocalDateTime.of(2020, 1, 1, 10, 0))).thenReturn(true);

        clientService.createAppointment(appointmentDTO);

        verify(appointmentRepository).save(any(Appointment.class));
        verify(availabilityRepository).deleteByAgentIdAndStartTime("agent1", LocalDateTime.of(2020, 1, 1, 10, 0));
    }

    @Test
    public void shouldThrowTimeNotAvailableExceptionWhenCreatingAppointment() {
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setAgentId("agent1");
        appointmentDTO.setServiceId("1");
        appointmentDTO.setStartTime(LocalDateTime.of(2020, 1, 1, 10, 0));

        when(availabilityRepository.existsByAgentIdAndStartTime("agent1", LocalDateTime.of(2020, 1, 1, 10, 0)))
                .thenReturn(false);

        assertThatThrownBy(() -> clientService.createAppointment(appointmentDTO))
                .isInstanceOf(TimeNotAvailableException.class);
    }

    @Test
    public void shouldFindAvailableDates() {
        Availability availability = new Availability();
        availability.setStartTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(22, 0)));
        availability.setEndTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(23, 0)));
        availability.setAgentId("agent1");

        when(availabilityRepository.findByAgentIdAndStartTimeAfter("agent1", LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN))).thenReturn(
                List.of(availability));

        Set<LocalDate> dates = clientService.findAvailableDates("agent1", LocalDate.now().plusDays(1));

        assertThat(dates).containsExactly(LocalDate.now().plusDays(1));
    }

    @Test
    public void shouldFindAvailableTimes() {
        Availability availability = new Availability();
        availability.setStartTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(22, 0)));
        availability.setEndTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(23, 0)));
        availability.setAgentId("agent1");

        when(availabilityRepository.findByAgentIdAndStartTimeBetween("agent1", LocalDateTime.of(LocalDate.now(), LocalTime.MIN),
                LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN))).thenReturn(
                List.of(availability));

        Set<LocalTime> times = clientService.findAvailableTimes("agent1", LocalDate.now());

        assertThat(times).containsExactly(LocalTime.of(22, 0));
    }

}