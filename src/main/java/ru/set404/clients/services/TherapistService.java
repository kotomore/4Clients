package ru.set404.clients.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.set404.clients.exceptions.AppointmentNotFoundException;
import ru.set404.clients.exceptions.TherapistNotFoundException;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Therapist;
import ru.set404.clients.repositories.TherapistsRepositorySQL;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class TherapistService {
    private final TherapistsRepositorySQL repository;

    @Autowired
    public TherapistService(TherapistsRepositorySQL repository) {
        this.repository = repository;
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

    public Therapist saveTherapist(Therapist therapist) {
        return repository.createTherapist(therapist);
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
        repository.addAvailableTime(therapistId, date, timeStart, timeEnd);
    }

}
