package ru.set404.clients.repositories;

import ru.set404.clients.dto.AppointmentDTO;
import ru.set404.clients.dto.AppointmentsForSiteDTO;
import ru.set404.clients.models.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    List<Appointment> findAppointmentsForTherapist(Long therapistId);

    List<AppointmentsForSiteDTO> findAppointmentsForTherapistSite(Long therapistId);

    Optional<Appointment> findAppointmentForTherapistById(Long therapistId, Long appointmentId);

    void createAppointment(Appointment appointment);

    List<LocalTime> findAppointmentsByDay(Long therapistId, LocalDate date);

    LocalDate deleteAppointment(Long therapistId, Long appointmentId);
}
