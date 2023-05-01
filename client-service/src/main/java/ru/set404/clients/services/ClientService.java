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
        if (!availabilityRepository.existsByAgentIdAndDateAndStartTime(
                appointmentDTO.getAgentId(),
                appointmentDTO.getStartTime().toLocalDate(),
                appointmentDTO.getStartTime().toLocalTime())) {
            throw new TimeNotAvailableException();
        }

        AgentService agentService = serviceRepository.findById(appointmentDTO.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service with id " + appointmentDTO.getServiceId() + " does not exist"));

        LocalDateTime startTime = appointmentDTO.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(agentService.getDuration());

        TimeSlot timeSlot = new TimeSlot(startTime.toLocalTime(), endTime.toLocalTime());

        Appointment appointment = new Appointment();
        appointment.setServiceId(appointmentDTO.getServiceId());
        appointment.setAgentId(appointmentDTO.getAgentId());
        appointment.setDate(appointmentDTO.getStartTime().toLocalDate());
        appointment.setTimeSlot(timeSlot);
        appointment.setClient(modelMapper.map(appointmentDTO.getClient(), Client.class));

        updateSchedule(appointmentDTO.getAgentId(), appointmentDTO.getStartTime().toLocalDate(), timeSlot);
        appointmentRepository.save(appointment);
        rabbitService.sendTelegramNotification(appointment);
    }

    private void updateSchedule(String agentId, LocalDate date, TimeSlot timeSlot) {
        availabilityRepository.deleteByAgentIdAndDateAndStartTime(agentId, date, timeSlot.getStartTime());
    }

    public Set<LocalDate> findAvailableDates(String agentId, LocalDate date) {
        Set<LocalDate> dates = availabilityRepository
                .findByAgentIdAndDateAfter(agentId, date.minusDays(1))
                .stream()
                .filter(availability -> {
                    if (availability.getDate().equals(LocalDate.now())) {
                        return !availability.getStartTime().isBefore(LocalTime.now());
                    } else {
                        return true;
                    }
                })
                .map(Availability::getDate)
                .collect(Collectors.toCollection(TreeSet::new));
        if (dates.isEmpty()) {
            throw new TimeNotAvailableException();
        }
        return dates;
    }

    public Set<LocalTime> findAvailableTimes(String agentId, LocalDate date) {
        Set<LocalTime> times = availabilityRepository
                .findByAgentIdAndDateBetween(agentId, date, date.plusDays(1))
                .stream()
                .filter(availability -> {
                    if (availability.getDate().equals(LocalDate.now())) {
                        return availability.getStartTime().isAfter(LocalTime.now());
                    } else {
                        return true;
                    }
                })
                .map(Availability::getStartTime)
                .filter(localTime -> localTime.isAfter(LocalTime.now()))
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
