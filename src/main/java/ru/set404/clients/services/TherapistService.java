package ru.set404.clients.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.set404.clients.exceptions.AppointmentNotFoundException;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.repositories.TherapistsRepositorySQL;

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
                .getAppointmentsForTherapist(therapistId).orElseThrow(() -> new AppointmentNotFoundException(therapistId));
    }
}
