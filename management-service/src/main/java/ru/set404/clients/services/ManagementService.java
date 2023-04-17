package ru.set404.clients.services;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.set404.clients.dto.AgentDTO;
import ru.set404.clients.dto.TimeSlotDTO;
import ru.set404.clients.exceptions.*;
import ru.set404.clients.models.*;
import ru.set404.clients.repositories.AgentRepository;
import ru.set404.clients.repositories.AppointmentRepository;
import ru.set404.clients.repositories.ScheduleRepository;
import ru.set404.clients.repositories.ServiceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ManagementService {
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;

    private final ModelMapper modelMapper;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;


    public List<Appointment> findAllAppointments(String agentId) {

        List<Appointment> appointments = appointmentRepository.findByAgentId(agentId);
        if (!appointments.isEmpty()) {
            return appointments;
        } else {
            throw new AppointmentNotFoundException(agentId);
        }
    }

    public Appointment findAppointmentById(String agentId, String appointmentId) {
        return appointmentRepository.findByIdAndAgentId(appointmentId, agentId)
                .orElseThrow(() -> new AppointmentNotFoundException(agentId));
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

    public void deleteAppointment(String agentId, String appointmentId) {
        appointmentRepository.findByIdAndAgentId(appointmentId, agentId);
    }

    public Agent findAgentById(String agentId) {
        return agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));
    }

    public AgentDTO findAgentDTOById(String agentId) {
        return modelMapper.map(agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId)), AgentDTO.class);
    }

    public void updateAgent(String agentId, AgentDTO agentDTO) {
        Agent agent = findAgentById(agentId);
        agent.setName(agentDTO.getName());
        agent.setPhone(agentDTO.getPhone());
        agent.setPassword(passwordEncoder.encode(agentDTO.getPassword()));
        agentRepository.save(agent);
    }

    public void addAvailableTime(String agentId, TimeSlotDTO timeSlotDTO) {
        List<TimeSlot> timeSlots = createTimeSlots(timeSlotDTO);

        for (LocalDate date = timeSlotDTO.getDateStart();
             date.isBefore(timeSlotDTO.getDateEnd());
             date = date.plusDays(1)) {

            scheduleRepository.deleteByAgentIdAndDate(agentId, date);
            Optional<Schedule> oldSchedule = scheduleRepository.findByAgentIdAndDate(agentId, date);

            Schedule schedule = new Schedule();
            oldSchedule.ifPresent(value -> schedule.setId(value.getId()));
            schedule.setDate(date);
            schedule.setAgentId(agentId);
            schedule.setAvailableSlots(timeSlots);
            scheduleRepository.save(schedule);
        }
    }

    private List<TimeSlot> createTimeSlots(TimeSlotDTO timeSlotDTO) {
        List<TimeSlot> timeSlots = new ArrayList<>();

        int serviceDuration = serviceRepository.findById(timeSlotDTO.getServiceId())
                .map(ru.set404.clients.models.AgentService::getDuration)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        for (LocalDateTime date = LocalDateTime.of(timeSlotDTO.getDateStart(), timeSlotDTO.getTimeStart());
             date.isBefore(LocalDateTime.of(timeSlotDTO.getDateEnd(), timeSlotDTO.getTimeEnd()));
             date = date.plusMinutes(serviceDuration)) {
            LocalDateTime end = date.plusMinutes(serviceDuration);
            TimeSlot timeSlot = new TimeSlot(date.toLocalTime(), end.toLocalTime());
            timeSlots.add(timeSlot);
        }
        return timeSlots;
    }

    public void deleteAvailableTime(String agentId, LocalDate date) {
        scheduleRepository.deleteByAgentIdAndDate(agentId, date);
    }

    public void deleteTherapist(String agentId) {
        agentRepository.deleteById(agentId);
    }

    public ru.set404.clients.models.AgentService findService(String agentId) {
        return serviceRepository.findByAgentId(agentId).orElseThrow(() -> new ServiceNotFoundException(agentId));
    }

    public List<Client> findClients(String agentId) {
        List<Client> clients = appointmentRepository.findByAgentId(agentId)
                .stream()
                .distinct()
                .map(Appointment::getClient)
                .collect(Collectors.toList());

        if (clients.size() > 0) {
            return clients;
        } else {
            throw new ClientNotFoundException(agentId);
        }
    }

    public void addOrUpdateService(AgentService service) {
        serviceRepository.save(service);
    }
}
