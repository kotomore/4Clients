package ru.set404.clients.services;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.set404.clients.dto.AgentDTO;
import ru.set404.clients.dto.AgentServiceDTO;
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
        Optional<Appointment> appointment = appointmentRepository.findByIdAndAgentId(appointmentId, agentId);
        Optional<Schedule> oldSchedule = scheduleRepository.findByAgentIdAndDate(agentId, appointment.map(Appointment::getDate)
                .orElseThrow(() -> new AppointmentNotFoundException(agentId)));

        oldSchedule.ifPresentOrElse(schedule -> {
            List<TimeSlot> timeSlots = new ArrayList<>(schedule.getAvailableSlots());
            appointment.map(Appointment::getTimeSlot).ifPresent(timeSlots::remove);
            Schedule updatedSchedule = new Schedule(schedule.getId(), agentId, schedule.getDate(), timeSlots);
            scheduleRepository.save(updatedSchedule);
        }, () -> {
            throw new RuntimeException("Schedule not found");
        });

        appointmentRepository.deleteByIdAndAgentId(appointmentId, agentId);
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
        try {
            agentRepository.save(agent);
        } catch (DuplicateKeyException exception) {
            throw new UserAlreadyExistException();
        }
    }

    @Transactional
    public void addAvailableTime(String agentId, TimeSlotDTO timeSlotDTO) {

        List<TimeSlot> timeSlots = createTimeSlots(timeSlotDTO);

        List<Appointment> appointments = appointmentRepository.findByAgentIdAndDateAfter(agentId, timeSlotDTO.getDateStart());
        List<LocalDateTime> appointedTime = appointments
                .stream()
                .map(appointment -> LocalDateTime.of(appointment.getDate(), appointment.getTimeSlot().getStartTime())).toList();

        for (LocalDate date = timeSlotDTO.getDateStart();
             !date.isAfter(timeSlotDTO.getDateEnd());
             date = date.plusDays(1)) {

            LocalDate finalDate = date;
            List<TimeSlot> timeSlotsWithoutAppointment = timeSlots.stream()
                    .filter(timeSlot -> !appointedTime.contains(LocalDateTime.of(finalDate, timeSlot.getStartTime())))
                    .collect(Collectors.toList());

            Optional<Schedule> oldSchedule = scheduleRepository
                    .findByAgentIdAndDate(agentId, date);

            Schedule schedule = new Schedule();
            oldSchedule.ifPresent(value -> schedule.setId(value.getId()));
            schedule.setDate(date);
            schedule.setAgentId(agentId);
            schedule.setAvailableSlots(timeSlotsWithoutAppointment);

            if (!timeSlotsWithoutAppointment.isEmpty()) {
                scheduleRepository.save(schedule);
            } else {
                scheduleRepository.deleteByAgentIdAndDate(agentId, date);
            }
        }
    }

    private List<TimeSlot> createTimeSlots(TimeSlotDTO timeSlotDTO) {
        List<TimeSlot> timeSlots = new ArrayList<>();

        int serviceDuration = serviceRepository.findById(timeSlotDTO.getServiceId())
                .map(AgentService::getDuration)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found"));

        for (LocalDateTime date = LocalDate.now().atTime(timeSlotDTO.getTimeStart());
             date.isBefore(LocalDate.now().atTime(timeSlotDTO.getTimeEnd()));
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

    public AgentService findService(String agentId) {
        return serviceRepository.findByAgentId(agentId).orElseThrow(() -> new ServiceNotFoundException(agentId));
    }

    public Agent findOrCreateAgentByPhone(String phone) {
        return agentRepository.findAgentByPhone(phone)
                .orElseGet(() -> {
                    Agent newAgent = new Agent();
                    newAgent.setPhone(phone);
                    return agentRepository.save(newAgent);
                });
    }

    public List<Client> findClients(String agentId) {
        List<Client> clients = appointmentRepository.findByAgentId(agentId)
                .stream()
                .distinct()
                .map(Appointment::getClient)
                .collect(Collectors.toList());

        if (!clients.isEmpty()) {
            return clients;
        } else {
            throw new ClientNotFoundException(agentId);
        }
    }

    public AgentService addOrUpdateService(String agentId, AgentServiceDTO service) {
        AgentService newAgentService = modelMapper.map(service, AgentService.class);
        newAgentService.setAgentId(agentId);

        Optional<AgentService> updatedAgentService = serviceRepository.findByAgentId(agentId);
        updatedAgentService.ifPresent(agentService -> newAgentService.setId(agentService.getId()));
        return serviceRepository.save(newAgentService);
    }
}
