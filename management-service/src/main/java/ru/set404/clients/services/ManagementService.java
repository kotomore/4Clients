package ru.set404.clients.services;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.set404.clients.dto.AgentDTO;
import ru.set404.clients.dto.AgentServiceDTO;
import ru.set404.clients.dto.TimeSlotDTO;
import ru.set404.clients.exceptions.*;
import ru.set404.clients.models.*;
import ru.set404.clients.repositories.AgentRepository;
import ru.set404.clients.repositories.AppointmentRepository;
import ru.set404.clients.repositories.AvailabilityRepository;
import ru.set404.clients.repositories.ServiceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ManagementService {
    private final AvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final ModelMapper modelMapper;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;
    private final int SCHEDULE_MAX_DAYS_COUNT = 30;

    public List<Appointment> findAllAppointments(String agentId) throws AppointmentNotFoundException {

        List<Appointment> appointments = appointmentRepository.findByAgentIdAndStartTimeAfter(agentId,
                LocalDateTime.now().minusDays(1), Sort.by("startTime"));
        if (!appointments.isEmpty()) {
            return appointments;
        } else {
            throw new AppointmentNotFoundException(agentId);
        }
    }

    public Appointment findAppointmentById(String agentId, String appointmentId) throws AgentNotFoundException {
        return appointmentRepository.findByIdAndAgentId(appointmentId, agentId)
                .orElseThrow(() -> new AppointmentNotFoundException(agentId));
    }

    public void deleteAppointment(String agentId, String appointmentId) throws RuntimeException {
        Appointment appointment = appointmentRepository.findByIdAndAgentId(appointmentId, agentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
        Availability availability = availabilityRepository.findByAgentIdAndStartTime(
                appointment.getAgentId(),
                appointment.getStartTime())
                        .orElseThrow(() -> new RuntimeException("Schedule not found"));

        availabilityRepository.deleteById(availability.getId());
        appointmentRepository.deleteByIdAndAgentId(appointmentId, agentId);
    }

    public Agent findAgentById(String agentId) throws AgentNotFoundException{
        return agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));
    }

    public AgentDTO findAgentDTOById(String agentId) throws AgentNotFoundException{
        return modelMapper.map(agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId)), AgentDTO.class);
    }

    public Agent updateAgent(String agentId, AgentDTO agentDTO) throws UserAlreadyExistException {
        Agent agent = findAgentById(agentId);

        if (agentDTO.getName() != null) agent.setName(agentDTO.getName());
        if (agentDTO.getPhone() != null) agent.setPhone(agentDTO.getPhone());
        if (agentDTO.getPassword() != null) agent.setPassword(passwordEncoder.encode(agentDTO.getPassword()));

        try {
           return agentRepository.save(agent);
        } catch (DuplicateKeyException exception) {
            throw new UserAlreadyExistException();
        }
    }

    public void addAvailableTime(String agentId, TimeSlotDTO timeSlotDTO) throws ServiceNotFoundException{
        LocalDateTime timeSlotStartDateTime = LocalDateTime.of(timeSlotDTO.getDateStart(),
                timeSlotDTO.getTimeStart());

        List<LocalDateTime> appointedTime = getAppointmentsTime(agentId, timeSlotStartDateTime.minusDays(1));

        AgentService service = serviceRepository.findByAgentId(agentId)
                .orElseThrow(() -> new ServiceNotFoundException(agentId));

        List<Availability> availabilityList = new ArrayList<>();
        for (LocalDate date = timeSlotDTO.getDateStart();
             date.isBefore(timeSlotDTO.getDateEnd().plusDays(1));
             date = date.plusDays(1)) {

            //Make schedule time for every day, exclude appointed time and old current schedules
            for (LocalTime time  = timeSlotDTO.getTimeStart();
                 time.isBefore(timeSlotDTO.getTimeEnd());
                 time = time.plusMinutes(service.getDuration())) {

                if (!appointedTime.contains(LocalDateTime.of(date, time))) {

                    Availability availability = getAvailability(agentId, service, date, time);
                    availabilityList.add(availability);
                }
            }
        }
        availabilityRepository.deleteByAgentIdAndStartTimeBetween(agentId,
                LocalDateTime.of(timeSlotDTO.getDateStart(), LocalTime.MIN),
                LocalDateTime.of(timeSlotDTO.getDateEnd(), LocalTime.MAX));
        availabilityRepository.saveAll(availabilityList);
    }

    public void deleteAllAvailableTime(String agentId) {
        availabilityRepository.deleteAllByAgentId(agentId);
    }

    private List<LocalDateTime> getAppointmentsTime(String agentId, LocalDateTime timeSlotStartDateTime) {
        return appointmentRepository
                .findByAgentIdAndStartTimeAfter(agentId, timeSlotStartDateTime, Sort.unsorted())
                .stream()
                .map(Appointment::getStartTime)
                .toList();
    }

    private static Availability getAvailability(String agentId, AgentService service, LocalDate date, LocalTime time) {
        LocalDateTime startTime = LocalDateTime.of(date, time);
        Availability availability = new Availability();
        availability.setStartTime(startTime);
        availability.setEndTime(startTime.plusMinutes(service.getDuration()));
        availability.setAgentId(agentId);
        return availability;
    }

    public Set<LocalTime> findAvailableTimes(String agentId, LocalDate date) throws TimeNotAvailableException{
        LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(date.plusDays(1), LocalTime.MIN);
        Set<LocalTime> times = availabilityRepository
                .findByAgentIdAndStartTimeBetween(agentId, startTime, endTime)
                .stream()
                .map(availability -> availability.getStartTime().toLocalTime())
                .collect(Collectors.toCollection(TreeSet::new));
        if (times.isEmpty()) {
            throw new TimeNotAvailableException();
        }
        return times;
    }

    public List<Availability> findAvailableTimeForTelegram(String agentId) {
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
       return availabilityRepository
               .findByAgentIdAndStartTimeBetween(agentId, startDateTime, startDateTime.plusDays(SCHEDULE_MAX_DAYS_COUNT),
                       Sort.by("startTime"));
    }

    public void deleteAvailableTime(String agentId, LocalDate date) {
        LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(date.plusDays(1), LocalTime.MIN);
        availabilityRepository.deleteByAgentIdAndStartTimeBetween(agentId, startTime, endTime);
    }

    public void deleteTherapist(String agentId) {
        agentRepository.deleteById(agentId);
    }

    public AgentService findService(String agentId) throws ServiceNotFoundException{
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

    public List<Client> findClients(String agentId) throws ClientNotFoundException{
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

    public AgentService addOrUpdateService(String agentId, AgentService service) {
        AgentService newAgentService = serviceRepository.findByAgentId(service.getAgentId()).orElse(new AgentService());
        if (service.getName() != null) newAgentService.setName(service.getName());
        if (service.getDescription() != null) newAgentService.setDescription(service.getDescription());
        if (service.getPrice() != 0d) newAgentService.setPrice(service.getPrice());
        if (service.getDuration() >= 15) newAgentService.setDuration(service.getDuration());

        newAgentService.setAgentId(agentId);

        serviceRepository.findByAgentId(agentId)
                .ifPresent(agentService -> newAgentService.setId(agentService.getId()));
        return serviceRepository.save(newAgentService);
    }
}
