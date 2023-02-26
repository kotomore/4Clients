package ru.set404.clients.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.set404.clients.dto.ServiceDTO;
import ru.set404.clients.dto.TherapistDTO;
import ru.set404.clients.exceptions.AppointmentNotFoundException;
import ru.set404.clients.exceptions.ServiceNotFoundException;
import ru.set404.clients.exceptions.TherapistNotFoundException;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Service;
import ru.set404.clients.models.Therapist;
import ru.set404.clients.repositories.TherapistsRepositorySQL;

import java.time.LocalDate;
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

    public Appointment addAppoinment(Appointment appointment) {
        if (repository.isTimeAvailable(appointment))
            repository.createAppointment(appointment);
        else return null;
        return appointment;
    }

    public List<Appointment> findAll(Long therapistId) {
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
        return repository.getAvailableTimes(therapistId, date);
    }

    public List<LocalDate> getAvailableDates(Long therapistId, LocalDate date) {
        return repository.getAvailableDates(therapistId, date);
    }

    public void deleteAppointment(Long appointmentId) {
        repository.deleteAppointment(appointmentId);
    }

    public Long saveTherapist(TherapistDTO therapist) {
        Therapist newTherapist = modelMapper.map(therapist, Therapist.class);
        newTherapist.setRole("Therapist");
        return repository.createTherapist(newTherapist);
    }

    public List<Therapist> getAllTherapist() {
        return repository.getAllTherapist();
    }

    public Therapist getTherapist(Long therapistId) {
        return repository
                .getTherapist(therapistId)
                .orElseThrow(() -> new TherapistNotFoundException(therapistId));
    }

    public void updateTherapist(Therapist therapist) {
        repository.updateTherapist(therapist);
    }

    public void addAvailableTime(Long therapistId, LocalDate date, LocalTime timeStart, LocalTime timeEnd) {
        repository.addorUpdateAvailableTime(therapistId, date, timeStart, timeEnd);
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

    public void addOrUpdateService(Long therapistId, ServiceDTO service) {
        Service updatedService = modelMapper.map(service, Service.class);
        repository.addOrUpdateService(therapistId, updatedService);
    }

}
