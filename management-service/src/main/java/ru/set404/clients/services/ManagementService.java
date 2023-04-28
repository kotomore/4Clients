package ru.set404.clients.services;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.set404.clients.dto.AgentDTO;
import ru.set404.clients.dto.AgentServiceDTO;
import ru.set404.clients.dto.TimeSlotDTO;
import ru.set404.clients.dto.telegram.AvailabilityMSG;
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

    public void deleteAppointment(String agentId, String appointmentId) {
        Appointment appointment = appointmentRepository.findByIdAndAgentId(appointmentId, agentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
        Availability availability = availabilityRepository.findByAgentIdAndDateAndStartTime(
                appointment.getAgentId(),
                appointment.getDate(),
                appointment.getTimeSlot().getStartTime())
                        .orElseThrow(() -> new RuntimeException("Schedule not found"));

        availabilityRepository.deleteById(availability.getId());
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

        if (agentDTO.getName() != null) agent.setName(agentDTO.getName());
        if (agentDTO.getPhone() != null) agent.setPhone(agentDTO.getPhone());
        if (agentDTO.getPassword() != null) agent.setPassword(passwordEncoder.encode(agentDTO.getPassword()));

        try {
            agentRepository.save(agent);
        } catch (DuplicateKeyException exception) {
            throw new UserAlreadyExistException();
        }
    }

    public void addAvailableTime(String agentId, TimeSlotDTO timeSlotDTO) {

        List<Appointment> appointments = appointmentRepository.findByAgentIdAndDateAfter(agentId, timeSlotDTO.getDateStart());
        List<LocalDateTime> appointedTime = appointments
                .stream()
                .map(appointment -> LocalDateTime.of(appointment.getDate(), appointment.getTimeSlot().getStartTime())).toList();

        AgentService service = serviceRepository.findByAgentId(agentId)
                .orElseThrow(() -> new ServiceNotFoundException(agentId));

        List<Availability> availabilityList = new ArrayList<>();
        for (LocalDate date = timeSlotDTO.getDateStart();
             date.isBefore(timeSlotDTO.getDateEnd().plusDays(1));
             date = date.plusDays(1)) {

            for (LocalTime time  = timeSlotDTO.getTimeStart();
                 time.isBefore(timeSlotDTO.getTimeEnd());
                 time = time.plusMinutes(service.getDuration())) {

                if (!appointedTime.contains(LocalDateTime.of(date, time))) {
                    Availability availability = new Availability();
                    availability.setDate(date);
                    availability.setStartTime(time);
                    availability.setEndTime(time.plusMinutes(service.getDuration()));
                    availability.setAgentId(agentId);

                    availabilityList.add(availability);
                }
            }
        }
        availabilityRepository.saveAll(availabilityList);
    }

    public AvailabilityMSG findAvailableTime(String agentId) {
       List<Availability> availabilityList = availabilityRepository
               .findByAgentIdAndDateAfter(agentId, LocalDate.now().minusDays(1));

       StringBuilder availabilities = new StringBuilder();
       LocalDate date = LocalDate.now().minusDays(1);
       for (Availability availability : availabilityList) {
           if (!date.equals(availability.getDate())) {
               availabilities.append("\n*").append("Дата: ").append(availability.getDate()).append("*\n\n");
               date = availability.getDate();
           }
           availabilities.append(availability.getStartTime()).append(" - ").append(availability.getEndTime()).append("\n");
       }
       AvailabilityMSG availabilityMSG = new AvailabilityMSG();
       availabilityMSG.setAgentId(agentId);
       availabilityMSG.setAvailability(availabilities.toString());
       return availabilityMSG;
    }

    public void deleteAvailableTime(String agentId, LocalDate date) {
        availabilityRepository.deleteByAgentIdAndDate(agentId, date);
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

    public void addOrUpdateService(String agentId, AgentService service) {
        AgentService newAgentService = serviceRepository.findByAgentId(service.getAgentId()).orElse(new AgentService());
        if (service.getName() != null) newAgentService.setName(service.getName());
        if (service.getDescription() != null) newAgentService.setDescription(service.getDescription());
        if (service.getPrice() != 0d) newAgentService.setPrice(service.getPrice());
        if (service.getDuration() != 0) newAgentService.setDuration(service.getDuration());

        newAgentService.setAgentId(agentId);

        Optional<AgentService> updatedAgentService = serviceRepository.findByAgentId(agentId);
        updatedAgentService.ifPresent(agentService -> newAgentService.setId(agentService.getId()));
        serviceRepository.save(newAgentService);
    }
}
