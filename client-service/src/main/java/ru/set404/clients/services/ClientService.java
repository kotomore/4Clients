package ru.set404.clients.services;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.set404.clients.dto.AgentServiceDTO;
import ru.set404.clients.dto.AppointmentDTO;
import ru.set404.clients.exceptions.AgentNotFoundException;
import ru.set404.clients.exceptions.AgentServiceNotFoundException;
import ru.set404.clients.exceptions.TimeNotAvailableException;
import ru.set404.clients.models.*;
import ru.set404.clients.repositories.AgentRepository;
import ru.set404.clients.repositories.AppointmentRepository;
import ru.set404.clients.repositories.AvailabilityRepository;
import ru.set404.clients.repositories.ServiceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final AgentRepository agentRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ModelMapper modelMapper;
    private final RabbitService rabbitService;


    @Transactional
    public void createAppointment(AppointmentDTO appointmentDTO) {
        if (!availabilityRepository.existsByAgentIdAndStartTime(
                appointmentDTO.getAgentId(),
                appointmentDTO.getStartTime())) {
            throw new TimeNotAvailableException();
        }

        AgentService agentService = serviceRepository.findById(appointmentDTO.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service with id " + appointmentDTO.getServiceId() + " does not exist"));

        LocalDateTime startTime = appointmentDTO.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(agentService.getDuration());

        Appointment appointment = new Appointment();
        appointment.setServiceId(appointmentDTO.getServiceId());
        appointment.setAgentId(appointmentDTO.getAgentId());
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setClient(modelMapper.map(appointmentDTO.getClient(), Client.class));

        updateSchedule(appointmentDTO.getAgentId(), startTime);
        appointmentRepository.save(appointment);
        rabbitService.sendTelegramNotification(appointment);
    }

    private void updateSchedule(String agentId, LocalDateTime startTime) {
        availabilityRepository.deleteByAgentIdAndStartTime(agentId, startTime);
    }

    public Set<LocalDate> findAvailableDates(String agentId, LocalDate date) {
        Set<LocalDate> dates = availabilityRepository
                .findByAgentIdAndStartTimeAfter(agentId, LocalDateTime.of(date, LocalTime.MIN))
                .stream()
                .filter(availability -> {
                    if (availability.getStartTime().toLocalDate().equals(LocalDate.now())) {
                        return availability.getStartTime().isAfter(LocalDateTime.now().plusHours(3));
                    } else {
                        return true;
                    }
                })
                .map(availability -> availability.getStartTime().toLocalDate())
                .collect(Collectors.toCollection(TreeSet::new));
        if (dates.isEmpty()) {
            throw new TimeNotAvailableException();
        }
        return dates;
    }

    public Set<LocalTime> findAvailableTimes(String agentId, LocalDate date) {
        LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(date.plusDays(1), LocalTime.MIN);

        Set<LocalTime> times = availabilityRepository
                .findByAgentIdAndStartTimeBetween(agentId, startTime, endTime)
                .stream()
                .filter(availability -> {
                    if (availability.getStartTime().toLocalDate().equals(LocalDate.now())) {
                        return availability.getStartTime().isAfter(LocalDateTime.now().plusHours(3));
                    } else {
                        return true;
                    }
                })
                .map(availability -> availability.getStartTime().toLocalTime())
                .collect(Collectors.toCollection(TreeSet::new));
        if (times.isEmpty()) {
            throw new TimeNotAvailableException();
        }
        return times;
    }

    public AgentServiceDTO findService(String agentId) {
        AgentService agentService = serviceRepository
                .findByAgentId(agentId)
                .orElseThrow(() -> new AgentServiceNotFoundException(agentId));

        AgentServiceDTO agentServiceDTO = modelMapper.map(agentService, AgentServiceDTO.class);

        Agent agent = agentRepository.findById(agentId).orElseThrow(() -> new AgentNotFoundException(agentId));
        agentServiceDTO.setAgentName(agent.getName());
        agentServiceDTO.setAgentPhone(agent.getPhone());

        return agentServiceDTO;
    }
}
