package ru.kotomore.managementservice.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.kotomore.managementservice.dto.AgentRequestDTO;
import ru.kotomore.managementservice.dto.AgentResponseDTO;
import ru.kotomore.managementservice.dto.AgentServiceDTO;
import ru.kotomore.managementservice.dto.TimeSlotDTO;
import ru.kotomore.managementservice.exceptions.*;
import ru.kotomore.managementservice.models.*;
import ru.kotomore.managementservice.repositories.*;

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
    private final SettingRepository settingRepository;

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

    public void deleteAppointment(String agentId, String appointmentId) throws AppointmentNotFoundException {
        Appointment appointment = appointmentRepository.findByIdAndAgentId(appointmentId, agentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
        Availability availability = new Availability();
        availability.setAgentId(agentId);
        availability.setStartTime(appointment.getStartTime());
        availability.setEndTime(appointment.getEndTime());

        availabilityRepository.save(availability);
        appointmentRepository.delete(appointment);
    }

    public void deleteAllAppointments(String agentId) throws AppointmentNotFoundException {
        List<Appointment> appointments = appointmentRepository.findByAgentId(agentId);
        List<Availability> availabilities = new ArrayList<>();

        for (Appointment appointment : appointments) {
            Availability availability = new Availability();
            availability.setAgentId(agentId);
            availability.setStartTime(appointment.getStartTime());
            availability.setEndTime(appointment.getEndTime());
            availabilities.add(availability);
        }

        availabilityRepository.saveAll(availabilities);
        appointmentRepository.deleteAllByAgentId(agentId);
    }

    public Agent findAgentById(String agentId) throws AgentNotFoundException {
        return agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));
    }

    public AgentResponseDTO findAgentDTOById(String agentId) throws AgentNotFoundException {
        return modelMapper.map(agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId)), AgentResponseDTO.class);
    }

    public Agent updateAgent(String agentId, AgentRequestDTO agentRequestDTO) throws UserAlreadyExistException {
        Agent agent = findAgentById(agentId);

        if (agentRequestDTO.getName() != null) agent.setName(agentRequestDTO.getName());
        if (agentRequestDTO.getPhone() != null) agent.setPhone(agentRequestDTO.getPhone());
        if (agentRequestDTO.getPassword() != null)
            agent.setPassword(passwordEncoder.encode(agentRequestDTO.getPassword()));

        try {
            return agentRepository.save(agent);
        } catch (DuplicateKeyException exception) {
            throw new UserAlreadyExistException();
        }
    }

    public void addAvailableTime(String agentId, TimeSlotDTO timeSlotDTO) throws ServiceNotFoundException {
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
            for (LocalTime time = timeSlotDTO.getTimeStart();
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

    private List<LocalDateTime> getAppointmentsTime(String agentId, LocalDateTime timeSlotStartDateTime) {
        return appointmentRepository
                .findByAgentIdAndStartTimeAfter(agentId, timeSlotStartDateTime.minusMinutes(1), Sort.unsorted())
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

    public void addBreak(String agentId, LocalDateTime startTime, LocalDateTime endTime) {
        List<LocalDateTime> appointedTime = getAppointmentsTime(agentId, startTime.minusMinutes(1));

        for (LocalDateTime date = startTime; date.isBefore(endTime);
             date = date.plusDays(1)) {
            if (!appointedTime.contains(date)) {
                availabilityRepository.deleteByAgentIdAndStartTimeBetween(agentId, date.minusMinutes(1),
                        LocalDateTime.of(date.toLocalDate(), endTime.toLocalTime()));
            } else {
                throw new AlreadyHaveAppointmentException();
            }
        }
    }

    public void deleteAllAvailableTime(String agentId) {
        availabilityRepository.deleteAllByAgentId(agentId);
    }

    public Set<LocalTime> findAvailableTimes(String agentId, LocalDate date) throws TimeNotAvailableException {
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
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        int SCHEDULE_MAX_DAYS_COUNT = 30;
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

    public AgentService findService(String agentId) throws ServiceNotFoundException {
        return serviceRepository.findByAgentId(agentId).orElseThrow(() -> new ServiceNotFoundException(agentId));
    }

    public Agent findOrCreateAgentByPhone(String phone) {
        String rawPassword = RandomStringUtils.random(6, true, true);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        return agentRepository.findAgentByPhone(phone)
                .orElseGet(() -> {
                    Agent newAgent = new Agent();
                    newAgent.setPhone(phone);
                    newAgent.setPassword(encodedPassword);
                    return agentRepository.save(newAgent);
                });
    }

    public List<Client> findClients(String agentId) throws ClientNotFoundException {
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

        return serviceRepository.save(newAgentService);
    }


    public AgentSettings getAgentSettings(String agentId) {
        return settingRepository.findByAgentId(agentId).orElse(new AgentSettings());
    }

    public AgentSettings addOrUpdateSettings(String agentId, AgentSettings settings) {
        String vanityUrl = settings.getVanityUrl().toLowerCase();
        if (settingRepository.existsByVanityUrl(vanityUrl)) {
            throw new UrlAlreadyExistException();
        }

        AgentSettings newAgentSettings = settingRepository.findByAgentId(settings.getAgentId())
                .orElse(new AgentSettings());
        if (settings.getVanityUrl() != null) newAgentSettings.setVanityUrl(vanityUrl);

        newAgentSettings.setAgentId(agentId);

        return settingRepository.save(newAgentSettings);
    }
}
