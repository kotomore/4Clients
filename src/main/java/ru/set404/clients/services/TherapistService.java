package ru.set404.clients.services;

import jakarta.security.auth.message.AuthException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.set404.clients.dto.AppointmentDTO;
import ru.set404.clients.dto.AppointmentsForSiteDTO;
import ru.set404.clients.dto.AvailabilitiesDTO;
import ru.set404.clients.dto.ServiceDTO;
import ru.set404.clients.exceptions.*;
import ru.set404.clients.models.*;
import ru.set404.clients.repositories.TherapistsRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class TherapistService {
    private final TherapistsRepository repository;
    private final ModelMapper modelMapper;

    @Autowired
    public TherapistService(TherapistsRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    public Appointment addAppointment(AppointmentDTO appointmentDTO) {
        Appointment appointment = appointmentDTO.toAppointment();
        if (repository.isTimeAvailable(appointment) && repository.findAvailableTimes(appointment.getTherapistId(),
                appointmentDTO.getStartTime().toLocalDate()).contains(appointmentDTO.getStartTime().toLocalTime()))
            repository.createAppointment(appointment);
        else {
            throw new TimeNotAvailableException();
        }
        return appointment;
    }

    public List<Appointment> findAllAppointments(Long therapistId) {
        return repository
                .findAppointmentsForTherapist(therapistId)
                .orElseThrow(() -> new AppointmentNotFoundException(therapistId));
    }

    public Appointment findAppointmentById(Long therapistId, Long appointmentId) {
        return repository
                .findAppointmentForTherapistById(therapistId, appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(therapistId));
    }

    public List<LocalTime> findAvailableTimes(Long therapistId, LocalDate date) {
        List<LocalTime> availableTimes = repository.findAvailableTimes(therapistId, date);
        if (availableTimes.size() > 0)
            return availableTimes;
        else
            throw new TimeNotAvailableException();
    }

    public List<LocalDate> findAvailableDates(Long therapistId, LocalDate date) {
        List<LocalDate> availableDates = repository.findAvailableDates(therapistId, date);
        if (availableDates.size() > 0)
            return availableDates;
        else
            throw new TimeNotAvailableException();
    }

    public void deleteAppointment(Long therapistId, Long appointmentId) {
        repository.deleteAppointment(therapistId, appointmentId);
    }

    public Therapist findTherapistById(Long therapistId) {
        return repository
                .findTherapistById(therapistId)
                .orElseThrow(() -> new TherapistNotFoundException(therapistId));
    }

    public Therapist findTherapistByPhone(String phone) throws AuthException {
        return repository
                .findTherapistByPhone(phone)
                .orElseThrow(() -> new AuthException(String.format("User with phone - %s not found", phone)));
    }

    public void updateTherapist(Therapist therapist) {
        repository.updateTherapist(therapist);
    }

    public void addAvailableTime(Long therapistId, Availability availability) {
        repository.addOrUpdateAvailableTime(therapistId, availability);
    }

    public void addAvailableTime(Long therapistId, AvailabilitiesDTO availabilitiesDTO) {
        repository.addOrUpdateAvailableTime(therapistId, availabilitiesDTO);
    }

    public void deleteAvailableTime(Long therapistId, LocalDate date) {
        repository.deleteAvailableTime(therapistId, date);
    }

    public void deleteTherapist(Long therapistId) {
        repository.deleteTherapist(therapistId);
    }

    public Service findService(Long therapistId) {
        return repository.findServiceByTherapist(therapistId).orElseThrow(() -> new ServiceNotFoundException(therapistId));
    }

    public List<Client> findClients(Long therapistId) {
        return repository.findClientsForTherapist(therapistId).orElseThrow(() -> new ClientNotFoundException(therapistId));
    }

    public void addOrUpdateService(Long therapistId, ServiceDTO service) {
        Service updatedService = modelMapper.map(service, Service.class);
        repository.addOrUpdateService(therapistId, updatedService);
    }

    public List<AppointmentsForSiteDTO> findAllAppointmentsDTO(Long therapistId) {
        return repository.findAppointmentsForTherapistSite(therapistId).orElseThrow(() -> new AppointmentNotFoundException(therapistId));
    }
}
