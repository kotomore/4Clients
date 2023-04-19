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
import ru.set404.clients.repositories.ScheduleRepository;
import ru.set404.clients.repositories.ServiceRepository;

import javax.management.ServiceNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final AgentRepository agentRepository;
    private final ModelMapper modelMapper;


    @Transactional
    public void createAppointment(AppointmentDTO appointmentDTO) {
        AgentService agentService = serviceRepository.findById(appointmentDTO.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service with id " + appointmentDTO.getServiceId() + " does not exist"));

        LocalDateTime startTime = appointmentDTO.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(agentService.getDuration());

        TimeSlot timeSlot = new TimeSlot(startTime.toLocalTime(), endTime.toLocalTime());

        Appointment appointment = new Appointment();
        appointment.setServiceId(appointmentDTO.getServiceId());
        appointment.setAgentId(appointmentDTO.getAgentId());
        appointment.setTimeSlot(timeSlot);
        appointment.setClient(modelMapper.map(appointmentDTO.getClient(), Client.class));

        appointmentRepository.save(appointment);
        updateSchedule(appointmentDTO.getAgentId(), appointmentDTO.getStartTime().toLocalDate(), timeSlot);
    }

    private void updateSchedule(String agentId, LocalDate date, TimeSlot timeSlot) {
        Schedule updatedSchedule = scheduleRepository.findByAgentIdAndDate(agentId, date).orElseThrow(TimeNotAvailableException::new);
        updatedSchedule.getAvailableSlots().remove(timeSlot);

        scheduleRepository.save(updatedSchedule);
    }


    public List<LocalDate> findAvailableDates(String agentId, LocalDate date) {
        List<LocalDate> dates = scheduleRepository
                .findByAgentIdAndDateGreaterThanEqual(agentId, date)
                .stream()
                .map(Schedule::getDate)
                .collect(Collectors.toList());
        if (dates.isEmpty()) {
            throw new TimeNotAvailableException();
        }
        return dates;
    }

    public AgentServiceDTO findService(String agentId) throws ServiceNotFoundException {
        AgentService agentService = serviceRepository
                .findByAgentId(agentId)
                .orElseThrow(() -> new AgentServiceNotFoundException(agentId));

        AgentServiceDTO agentServiceDTO = modelMapper.map(agentService, AgentServiceDTO.class);

        Agent agent = agentRepository.findById(agentId).orElseThrow(() -> new AgentNotFoundException(agentId));
        agentServiceDTO.setAgentName(agent.getName());
        agentServiceDTO.setAgentPhone(agent.getPhone());

        return agentServiceDTO;
    }

    public List<LocalTime> findAvailableTimes(String agentId, LocalDate date) {
        List<LocalTime> times;

        Optional<Schedule> schedules = scheduleRepository.findByAgentIdAndDate(agentId, date);
        if (schedules.isPresent()) {
            times = schedules.get().getAvailableSlots()
                    .stream()
                    .map(TimeSlot::getStartTime)
                    .collect(Collectors.toList());
        } else {
            throw new TimeNotAvailableException();
        }
        return times;
    }
}
