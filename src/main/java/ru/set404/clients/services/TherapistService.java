package ru.set404.clients.services;

import jakarta.security.auth.message.AuthException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.set404.clients.dto.AppointmentDTO;
import ru.set404.clients.dto.AppointmentsForSiteDTO;
import ru.set404.clients.dto.ServiceDTO;
import ru.set404.clients.dto.TherapistDTO;
import ru.set404.clients.exceptions.*;
import ru.set404.clients.models.*;
import ru.set404.clients.repositories.TherapistsRepositorySQL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class TherapistService {
    private final TherapistsRepositorySQL repository;
    private final ModelMapper modelMapper;

    @Autowired
    public TherapistService(TherapistsRepositorySQL repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    public Appointment addAppointment(AppointmentDTO appointmentDTO) {
        Appointment appointment = appointmentDTO.toAppointment();
        if (repository.isTimeAvailable(appointment) && repository.getAvailableTimes(appointment.getTherapistId(),
                appointmentDTO.getStartTime().toLocalDate()).contains(appointmentDTO.getStartTime().toLocalTime()))
            repository.createAppointment(appointment);
        else {
            throw new TimeNotAvailableException();
        }
        return appointment;
    }

    public List<Appointment> findAllAppointments(Long therapistId) {
        return repository
                .getAppointmentsForTherapist(therapistId)
                .orElseThrow(() -> new AppointmentNotFoundException(therapistId));
    }

    public Appointment getAppointmentById(Long therapistId, Long appointmentId) {
        return repository
                .getAppointmentForTherapistById(therapistId, appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(therapistId));
    }

    public List<LocalTime> getAvailableTimes(Long therapistId, LocalDate date) {
        List<LocalTime> availableTimes = repository.getAvailableTimes(therapistId, date);
        if (availableTimes.size() > 0)
            return availableTimes;
        else
            throw new TimeNotAvailableException();
    }

    public List<LocalDate> getAvailableDates(Long therapistId, LocalDate date) {
        List<LocalDate> availableDates = repository.getAvailableDates(therapistId, date);
        if (availableDates.size() > 0)
            return availableDates;
        else
            throw new TimeNotAvailableException();
    }

    public void deleteAppointment(Long therapistId, Long appointmentId) {
        repository.deleteAppointment(therapistId, appointmentId);
    }

    public Long saveTherapist(TherapistDTO therapist) {
        Therapist newTherapist = modelMapper.map(therapist, Therapist.class);
        newTherapist.setRole(Role.USER);
        return repository.createTherapist(newTherapist);
    }

    public List<Therapist> getAllTherapist() {
        return repository.getAllTherapist();
    }

    public Therapist getTherapist(Long therapistId) {
        return repository
                .getTherapistById(therapistId)
                .orElseThrow(() -> new TherapistNotFoundException(therapistId));
    }

    public Therapist getTherapist(String phone) throws AuthException {
        return repository
                .getTherapistByPhone(phone)
                .orElseThrow(() -> new AuthException(String.format("User with phone - %s not found", phone)));
    }

    public void updateTherapist(Therapist therapist) {
        repository.updateTherapist(therapist);
    }

    public void addAvailableTime(Long therapistId, LocalDate date, LocalTime timeStart, LocalTime timeEnd) {
        repository.addOrUpdateAvailableTime(therapistId, date, timeStart, timeEnd);
    }

    public void addAvailableTime(Long therapistId, LocalDateTime timeStart, LocalDateTime timeEnd) {
        repository.addOrUpdateAvailableTime(therapistId, timeStart, timeEnd);
    }

    public void deleteAvailableTime(Long therapistId, LocalDate date) {
        repository.deleteAvailableTime(therapistId, date);
    }

    public void deleteTherapist(Long therapistId) {
        repository.deleteTherapist(therapistId);
    }

    public Service getService(Long therapistId) {
        return repository.getServiceByTherapist(therapistId).orElseThrow(() -> new ServiceNotFoundException(therapistId));
    }

    public List<Client> getClients(Long therapistId) {
        return repository.getClientsForTherapist(therapistId).orElseThrow(() -> new ClientNotFoundException(therapistId));
    }

    public void addOrUpdateService(Long therapistId, ServiceDTO service) {
        Service updatedService = modelMapper.map(service, Service.class);
        repository.addOrUpdateService(therapistId, updatedService);
    }

    public List<AppointmentsForSiteDTO> findAllAppointmentsDTO(Long therapistId) {
        return repository.getAppointmentsForTherapistSite(therapistId).orElseThrow(() -> new AppointmentNotFoundException(therapistId));
    }
}
