package ru.set404.clients.repositories;

import ru.set404.clients.dto.AppointmentsForSiteDTO;
import ru.set404.clients.dto.AvailabilitiesDTO;
import ru.set404.clients.models.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TherapistsRepository {
    Long createTherapist(Therapist therapist);

    Optional<Client> findClientByPhoneNumber(String phoneNumber);

    Client createClient(Client client);

    boolean isTimeAvailable(Appointment appointment);

    void createAppointment(Appointment appointment);

    Optional<List<Appointment>> findAppointmentsForTherapist(Long therapistId);

    Optional<List<AppointmentsForSiteDTO>> findAppointmentsForTherapistSite(Long therapistId);

    Client makeClientFromResultSet(ResultSet resultSet) throws SQLException;

    Optional<List<Client>> findClientsForTherapist(Long therapistId);

    Optional<Appointment> findAppointmentForTherapistById(Long therapistId, Long appointmentId);

    List<LocalTime> findAppointmentsByDay(Long therapistId, LocalDate date);

    void markAvailabilityAs(Long therapistId, LocalDate date, boolean markAs);

    List<LocalTime> findAvailableTimes(Long therapistId, LocalDate date);

    List<LocalDate> findAvailableDates(Long therapistId, LocalDate date);

    void deleteAppointment(Long therapistId, Long appointmentId);

    Therapist makeTherapistFromResultSet(ResultSet resultSet) throws SQLException;

    Optional<Therapist> findTherapistById(Long therapistId);

    Optional<Therapist> findTherapistByPhone(String phone);

    void updateTherapist(Therapist therapist);

    boolean isHaveAvailableTime(Long therapistId, LocalDate date);

    void addOrUpdateAvailableTime(Long therapistId, Availability availability);

    void addOrUpdateAvailableTime(Long therapistId, AvailabilitiesDTO availabilitiesDTO);

    void deleteAvailableTime(Long therapistId, LocalDate date);

    void deleteTherapist(Long therapistId);

    Optional<Service> findServiceByTherapist(Long therapistId);

    void addOrUpdateService(Long therapistId, Service service);
}
