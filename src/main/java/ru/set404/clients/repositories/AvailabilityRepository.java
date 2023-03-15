package ru.set404.clients.repositories;

import ru.set404.clients.dto.AvailabilitiesDTO;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Availability;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AvailabilityRepository {
    boolean isTimeAvailable(Appointment appointment);

    List<LocalTime> findAvailableTimes(Long therapistId, LocalDate date, List<LocalTime> appointedTime);

    List<LocalDate> findAvailableDates(Long therapistId, LocalDate date, List<LocalTime> appointedTime);

    boolean isHaveAvailableTime(Long therapistId, LocalDate date);

    void addOrUpdateAvailableTime(Long therapistId, Availability availability, List<LocalTime> appointedTime);

    void addOrUpdateAvailableTime(Long therapistId, AvailabilitiesDTO availabilitiesDTO, List<LocalTime> appointedTime);

    void deleteAvailableTime(Long therapistId, LocalDate date);

    void markAvailabilityAs(Long therapistId, LocalDate date, boolean markAs);
}
