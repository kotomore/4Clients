package ru.set404.clients.services;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.set404.clients.dto.AppointmentDTO;
import ru.set404.clients.models.*;
import ru.set404.clients.repositories.AppointmentRepository;
import ru.set404.clients.repositories.ScheduleRepository;
import ru.set404.clients.repositories.ServiceRepository;

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
    private final ModelMapper modelMapper;


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
    }


    public List<LocalDate> findAvailableDates(String agentId, LocalDate date) {
        return scheduleRepository
                .findByAgentIdAndDateGreaterThanEqual(agentId, date)
                .stream()
                .map(Schedule::getDate)
                .collect(Collectors.toList());
    }

    public List<LocalTime> findAvailableTimes(String agentId, LocalDate date) {
        List<Schedule> schedules = scheduleRepository.findByAgentIdAndDate(agentId, date);
        return schedules.stream()
                .flatMap(schedule -> Optional.ofNullable(schedule.getAvailableSlots())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(TimeSlot::getStartTime))
                .collect(Collectors.toList());
    }
}
