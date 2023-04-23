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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final AgentRepository agentRepository;
    private final ModelMapper modelMapper;
    private final RabbitService rabbitService;


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
        appointment.setDate(appointmentDTO.getStartTime().toLocalDate());
        appointment.setTimeSlot(timeSlot);
        appointment.setClient(modelMapper.map(appointmentDTO.getClient(), Client.class));

        updateSchedule(appointmentDTO.getAgentId(), appointmentDTO.getStartTime().toLocalDate(), timeSlot);
        appointmentRepository.save(appointment);
        rabbitService.sendTelegramNotification(appointment);
    }

    private void updateSchedule(String agentId, LocalDate date, TimeSlot timeSlot) {
        Schedule updatedSchedule = scheduleRepository.findByAgentIdAndDate(agentId, date).orElseThrow(TimeNotAvailableException::new);
        if (updatedSchedule.getAvailableSlots().contains(timeSlot)) {
            updatedSchedule.getAvailableSlots().remove(timeSlot);
            if (updatedSchedule.getAvailableSlots().isEmpty()) {
                scheduleRepository.delete(updatedSchedule);
            } else {
                scheduleRepository.save(updatedSchedule);
            }
        } else {
            throw new TimeNotAvailableException();
        }

    }

    public List<LocalDate> findAvailableDates(String agentId, LocalDate date) {
        List<Schedule> schedules = scheduleRepository
                .findByAgentIdAndDateAfter(agentId, date);

        List<LocalDate> dates = schedules
                .stream()
                .map(Schedule::getDate)
                .collect(Collectors.toList());

        for (Schedule schedule : schedules) {
            if (schedule.getAvailableSlots().isEmpty()) {
                dates.remove(schedule.getDate());
                continue;
            }
            if (schedule.getDate().equals(LocalDate.now())) {
                int count = 0;
                for (TimeSlot timeSlot : schedule.getAvailableSlots()) {
                    if (timeSlot.getStartTime().isAfter(LocalTime.now())) {
                        count++;
                        break;
                    }
                }
                if (count == 0) {
                    dates.remove(schedule.getDate());
                }
            }
        }

        if (dates.isEmpty()) {
            throw new TimeNotAvailableException();
        }
        return dates;
    }

    public List<LocalTime> findAvailableTimes(String agentId, LocalDate date) {
        List<LocalTime> times = scheduleRepository.findByAgentIdAndDate(agentId, date)
                .map(schedule -> schedule.getAvailableSlots()
                        .stream()
                        .map(TimeSlot::getStartTime)
                        .filter(startTime -> date.isAfter(LocalDate.now()) || (date.isEqual(LocalDate.now()) && startTime.isAfter(LocalTime.now())))
                        .collect(Collectors.toList()))
                .orElseThrow(TimeNotAvailableException::new);
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
