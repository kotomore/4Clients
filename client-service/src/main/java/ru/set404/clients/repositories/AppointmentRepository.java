package ru.set404.clients.repositories;

import ru.set404.clients.models.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    List<Appointment> findAppointmentsForTherapist(Long therapistId);

    Optional<Appointment> findAppointmentForTherapistById(Long therapistId, Long appointmentId);

    List<LocalTime> findAppointmentsByDay(Long therapistId, LocalDate date);

    void createAppointment(Appointment appointment);

}
