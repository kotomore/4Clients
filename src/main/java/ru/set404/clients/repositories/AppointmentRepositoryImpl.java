package ru.set404.clients.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.set404.clients.dto.AppointmentsForSiteDTO;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Client;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AppointmentRepositoryImpl implements AppointmentRepository {

    @Value("${db.url}")
    private String DB_URL;
    @Value("${db.user}")
    private String DB_USER;
    @Value("${db.password}")
    private String DB_PASSWORD;

    @Override
    public List<Appointment> findAppointmentsForTherapist(Long therapistId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments " +
                "JOIN CLIENTS C on C.CLIENT_ID = APPOINTMENTS.CLIENT_ID " +
                "JOIN SERVICES S on S.SERVICE_ID = APPOINTMENTS.SERVICE_ID " +
                "WHERE APPOINTMENTS.therapist_id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setLong(1, therapistId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Long appointmentId = resultSet.getLong("appointment_id");
                    Long serviceId = resultSet.getLong("service_id");
                    Timestamp startTime = resultSet.getTimestamp("start_time");
                    Client client = makeClientFromResultSet(resultSet);
                    Appointment appointment = new Appointment(appointmentId, startTime.toLocalDateTime(), serviceId, therapistId, client);
                    appointments.add(appointment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    @Override
    public List<AppointmentsForSiteDTO> findAppointmentsForTherapistSite(Long therapistId) {
        List<AppointmentsForSiteDTO> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments " +
                "JOIN CLIENTS C on C.CLIENT_ID = APPOINTMENTS.CLIENT_ID " +
                "JOIN SERVICES S on S.SERVICE_ID = APPOINTMENTS.SERVICE_ID " +
                "WHERE APPOINTMENTS.therapist_id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, therapistId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Long appointmentId = resultSet.getLong("appointment_id");
                    Timestamp startTime = resultSet.getTimestamp("start_time");
                    int duration = resultSet.getInt("duration");
                    String clientName = resultSet.getString("name");
                    String clientPhone = resultSet.getString("phone");
                    AppointmentsForSiteDTO appointment = new AppointmentsForSiteDTO();
                    appointment.setId(appointmentId);
                    appointment.setTitle(clientName);
                    appointment.setStart(startTime.toLocalDateTime());
                    appointment.setEnd(startTime.toLocalDateTime().plusMinutes(duration));
                    appointment.setCategory(clientPhone);
                    appointments.add(appointment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    @Override
    public Optional<Appointment> findAppointmentForTherapistById(Long therapistId, Long appointmentId) {
        Appointment appointment = null;
        String sql = "SELECT * FROM appointments " +
                "JOIN CLIENTS C on C.CLIENT_ID = APPOINTMENTS.CLIENT_ID " +
                "JOIN SERVICES S on S.SERVICE_ID = APPOINTMENTS.SERVICE_ID " +
                "WHERE APPOINTMENTS.therapist_id = ? AND APPOINTMENT_ID = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, therapistId);
            statement.setLong(2, appointmentId);
            try (ResultSet resultSet = statement.executeQuery();){
                if (resultSet.next()) {
                    Long serviceId = resultSet.getLong("service_id");
                    Timestamp startTime = resultSet.getTimestamp("start_time");
                    Client client = makeClientFromResultSet(resultSet);
                    appointment = new Appointment(appointmentId, startTime.toLocalDateTime(), serviceId, therapistId, client);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (appointment != null)
            return Optional.of(appointment);
        else return Optional.empty();
    }

    @Override
    public void createAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointments (client_id, therapist_id, service_id, start_time) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
            statement.setLong(1, appointment.getClient().getId());
            statement.setLong(2, appointment.getTherapistId());
            statement.setLong(3, appointment.getServiceId());
            statement.setTimestamp(4, Timestamp.valueOf(appointment.getStartTime()));
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    appointment.setAppointmentId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating client failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<LocalTime> findAppointmentsByDay(Long therapistId, LocalDate date) {
        List<LocalTime> appointments = new ArrayList<>();
        String sql = "SELECT START_TIME FROM appointments " +
                "WHERE therapist_id = ? AND FORMATDATETIME(start_time, 'yyyy-MM-dd', 'de') = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, therapistId);
            statement.setDate(2, Date.valueOf(date));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    LocalDateTime startTime = resultSet.getTimestamp("start_time").toLocalDateTime();
                    appointments.add(startTime.toLocalTime());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    @Override
    public LocalDate deleteAppointment(Long therapistId, Long appointmentId) {
        LocalDate date = null;
        String sql = "SELECT * FROM appointments WHERE appointment_id = ? AND THERAPIST_ID = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, appointmentId);
            statement.setLong(2, therapistId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                date = resultSet.getTimestamp("start_time").toLocalDateTime().toLocalDate();
                sql = "DELETE FROM appointments WHERE appointment_id = ? AND THERAPIST_ID = ?";
                statement = connection.prepareStatement(sql);
                statement.setLong(1, appointmentId);
                statement.setLong(2, therapistId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return date;
    }

    private Client makeClientFromResultSet(ResultSet resultSet) throws SQLException {
        Long clientId = resultSet.getLong("client_id");
        String clientName = resultSet.getString("name");
        String clientPhone = resultSet.getString("phone");
        return new Client(clientId, clientName, clientPhone);
    }
}
