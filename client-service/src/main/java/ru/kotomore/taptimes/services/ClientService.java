package ru.kotomore.taptimes.services;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kotomore.taptimes.exceptions.AgentNotFoundException;
import ru.kotomore.taptimes.exceptions.AgentServiceNotFoundException;
import ru.kotomore.taptimes.exceptions.TimeNotAvailableException;
import ru.kotomore.taptimes.models.Agent;
import ru.kotomore.taptimes.models.AgentService;
import ru.kotomore.taptimes.models.Appointment;
import ru.kotomore.taptimes.models.Client;
import ru.kotomore.taptimes.repositories.AgentRepository;
import ru.kotomore.taptimes.repositories.AppointmentRepository;
import ru.kotomore.taptimes.repositories.AvailabilityRepository;
import ru.kotomore.taptimes.repositories.ServiceRepository;
import ru.kotomore.taptimes.dto.AgentServiceDTO;
import ru.kotomore.taptimes.dto.AppointmentDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
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
    private static final ZoneOffset TIMEZONE_OFFSET = ZoneOffset.of("+03:00");


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
        appointment = appointmentRepository.save(appointment);
        rabbitService.sendTelegramNotification(appointment);
    }

    private void updateSchedule(String agentId, LocalDateTime startTime) {
        availabilityRepositorypackage ru.kotomore.taptimes.services;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kotomore.taptimes.exceptions.AgentNotFoundException;
import ru.kotomore.taptimes.exceptions.AgentServiceNotFoundException;
import ru.kotomore.taptimes.exceptions.TimeNotAvailableException;
import ru.kotomore.taptimes.models.Agent;
import ru.kotomore.taptimes.models.AgentService;
import ru.kotomore.taptimes.models.Appointment;
import ru.kotomore.taptimes.models.Client;
import ru.kotomore.taptimes.repositories.AgentRepository;
import ru.kotomore.taptimes.repositories.AppointmentRepository;
import ru.kotomore.taptimes.repositories.AvailabilityRepository;
import ru.kotomore.taptimes.repositories.ServiceRepository;
import ru.kotomore.taptimes.dto.AgentServiceDTO;
import ru.kotomore.taptimes.dto.AppointmentDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
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
            private static final ZoneOffset TIMEZONE_OFFSET = ZoneOffset.of("+03:00");


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
                appointment = appointmentRepository.save(appointment);
                rabbitService.sendTelegramNotification(appointment);
            }

            private void updateSchedule(String agentId, LocalDateTime startTime) {
                availabilityRepository.deleteByAgentIdAndStartTime(agentId, startTime);
            }

            public Set<LocalDate> findAvailableDates(String agentId, LocalDate date) {
                LocalDateTime currentTimeWithOffset = getCurrentDateTimeWithOffset();

                Set<LocalDate> dates = availabilityRepository
                        .findByAgentIdAndStartTimeAfter(agentId, LocalDateTime.of(date, LocalTime.MIN))
                        .stream()
                        .filter(availability -> {
                            if (availability.getStartTime().toLocalDate().equals(LocalDate.now())) {
                                return availability.getStartTime().isAfter(currentTimeWithOffset);
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
                LocalDateTime currentTimeWithOffset = getCurrentDateTimeWithOffset();

                LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime endTime = LocalDateTime.of(date.plusDays(1), LocalTime.MIN);

                Set<LocalTime> times = availabilityRepository
                        .findByAgentIdAndStartTimeBetween(agentId, startTime, endTime)
                        .stream()
                        .filter(availability -> {
                            if (availability.getStartTime().toLocalDate().equals(LocalDate.now())) {
                                return availability.getStartTime().isAfter(currentTimeWithOffset);
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

            private static LocalDateTime getCurrentDateTimeWithOffset() {
                return LocalDateTime.now().atOffset(ZoneOffset.UTC)
                        .withOffsetSameInstant(TIMEZONE_OFFSET).toLocalDateTime();
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
.deleteByAgentIdAndStartTime(agentId, startTime);
    }

    public Set<LocalDate> findAvailableDates(String agentId, LocalDate date) {
        LocalDateTime currentTimeWithOffset = getCurrentDateTimeWithOffset();

        Set<LocalDate> dates = availabilityRepository
                .findByAgentIdAndStartTimeAfter(agentId, LocalDateTime.of(date, LocalTime.MIN))
                .stream()
                .filter(availability -> {
                    if (availability.getStartTime().toLocalDate().equals(LocalDate.now())) {
                        return availability.getStartTime().isAfter(currentTimeWithOffset);
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
        LocalDateTime currentTimeWithOffset = getCurrentDateTimeWithOffset();

        LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(date.plusDays(1), LocalTime.MIN);

        Set<LocalTime> times = availabilityRepository
                .findByAgentIdAndStartTimeBetween(agentId, startTime, endTime)
                .stream()
                .filter(availability -> {
                    if (availability.getStartTime().toLocalDate().equals(LocalDate.now())) {
                        return availability.getStartTime().isAfter(currentTimeWithOffset);
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

    private static LocalDateTime getCurrentDateTimeWithOffset() {
        return LocalDateTime.now().atOffset(ZoneOffset.UTC)
                .withOffsetSameInstant(TIMEZONE_OFFSET).toLocalDateTime();
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
