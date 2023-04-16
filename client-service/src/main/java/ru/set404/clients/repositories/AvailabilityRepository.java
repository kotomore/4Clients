package ru.set404.clients.repositories;

import ru.set404.clients.models.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AvailabilityRepository {
    boolean isTimeAvailable(Appointment appointment);

    List<LocalTime> findAvailableTimes(Long therapistId, LocalDate date, List<LocalTime> appointedTime);

    List<LocalDate> findAvailableDates(Long therapistId, LocalDate date, List<LocalTime> appointedTime);

    void markAvailabilityAs(Long therapistId, LocalDate date, boolean markAs);
}
