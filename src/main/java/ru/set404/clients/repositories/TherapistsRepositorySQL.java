package ru.set404.clients.repositories;

import org.springframework.stereotype.Repository;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Client;
import ru.set404.clients.models.Therapist;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TherapistsRepositorySQL {
    private static final String DB_URL = "jdbc:h2:file:/Users/kot/Java/clients/src/main/resources/static/sampledata";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "password";

    public Therapist createTherapist(Therapist therapist) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO therapists (name, phone, password, role) " +
                    "VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, therapist.getName());
            statement.setString(2, therapist.getPhone());
            statement.setString(3, therapist.getPassword());
            statement.setString(4, therapist.getRole());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    therapist.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating client failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return therapist;
    }

    public Optional<Client> getClientByPhoneNumber(String phoneNumber) {
        Optional<Client> client = Optional.empty();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM clients WHERE phone = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, phoneNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Long clientId = resultSet.getLong("client_id");
                String name = resultSet.getString("name");
                String phone = resultSet.getString("phone");
                client = Optional.of(new Client(clientId, name, phone));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return client;
    }

    public Client addClient(Client client) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO clients (name, phone) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, client.getName());
            statement.setString(2, client.getPhone());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    client.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating client failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return client;
    }

    public boolean isTimeAvailable(Appointment appointment) {
        boolean isAvailable = true;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM appointments WHERE therapist_id = ? AND start_time = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, appointment.getTherapistId());
            statement.setTimestamp(2, appointment.getStartTime());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                isAvailable = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isAvailable;
    }

    public void createAppointment(Appointment appointment) {

        Optional<Client> client = getClientByPhoneNumber(appointment.getClient().getPhone());
        appointment.setClient(client.orElse(addClient(appointment.getClient())));

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO appointments (client_id, therapist_id, service_id, start_time) " +
                    "VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, appointment.getClient().getId());
            statement.setLong(2, appointment.getTherapistId());
            statement.setLong(3, appointment.getServiceId());
            statement.setTimestamp(4, appointment.getStartTime());
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

    public Optional<List<Appointment>> getAppointmentsForTherapist(Long therapistId) {
        List<Appointment> appointments = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM appointments " +
                    "JOIN CLIENTS C on C.CLIENT_ID = APPOINTMENTS.CLIENT_ID " +
                    "JOIN SERVICES S on S.SERVICE_ID = APPOINTMENTS.SERVICE_ID " +
                    "WHERE APPOINTMENTS.therapist_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long appointmentId = resultSet.getLong("appointment_id");
                Long clientId = resultSet.getLong("client_id");
                Long serviceId = resultSet.getLong("service_id");
                Timestamp startTime = resultSet.getTimestamp("start_time");
                String clientName = resultSet.getString("name");
                String clientPhone = resultSet.getString("phone");
                Client client = new Client(clientId, clientName, clientPhone);
                Appointment appointment = new Appointment(appointmentId, startTime, serviceId, therapistId, client);
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (appointments.size() > 0)
            return Optional.of(appointments);
        else return Optional.empty();
    }

    public Optional<Appointment> getAppointmentForTherapistById(Long therapistId, Long appointmentId) {
        Appointment appointment = null;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM appointments " +
                    "JOIN CLIENTS C on C.CLIENT_ID = APPOINTMENTS.CLIENT_ID " +
                    "JOIN SERVICES S on S.SERVICE_ID = APPOINTMENTS.SERVICE_ID " +
                    "WHERE APPOINTMENTS.therapist_id = ? AND APPOINTMENT_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.setLong(2, appointmentId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Long clientId = resultSet.getLong("client_id");
                Long serviceId = resultSet.getLong("service_id");
                Timestamp startTime = resultSet.getTimestamp("start_time");
                String clientName = resultSet.getString("name");
                String clientPhone = resultSet.getString("phone");
                Client client = new Client(clientId, clientName, clientPhone);
                appointment = new Appointment(appointmentId, startTime, serviceId, therapistId, client);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (appointment != null)
            return Optional.of(appointment);
        else return Optional.empty();
    }


    private List<LocalTime> getAppointmentsByDay(Long therapistId, LocalDate date) {
        List<LocalTime> appointments = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT START_TIME FROM appointments " +
                    "WHERE therapist_id = ? AND FORMATDATETIME(start_time, 'yyyy-MM-dd') = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.setDate(2, Date.valueOf(date));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Timestamp startTime = resultSet.getTimestamp("start_time");
                appointments.add(startTime.toLocalDateTime().toLocalTime());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public List<LocalTime> getAvailableTimes(Long therapistId, LocalDate date) {
        List<LocalTime> availableTimes = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT START_TIME, END_TIME, DURATION FROM AVAILABILITY " +
                    "JOIN SERVICES ON AVAILABILITY.THERAPIST_ID = SERVICES.THERAPIST_ID " +
                    "WHERE AVAILABILITY.therapist_id = ? AND FORMATDATETIME(start_time, 'yyyy-MM-dd') = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.setDate(2, Date.valueOf(date));
            ResultSet resultSet = statement.executeQuery();

            List<LocalTime> appointedTime = getAppointmentsByDay(therapistId, date);

            if (resultSet.next()) {
                LocalDateTime startTime = resultSet.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime endTime = resultSet.getTimestamp("end_time").toLocalDateTime();
                int duration = resultSet.getInt("duration");
                for (LocalDateTime time = startTime; time.isBefore(endTime); time = time.plusMinutes(duration)) {
                    if (!appointedTime.contains(time.toLocalTime())) {
                        availableTimes.add(time.toLocalTime());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableTimes;
    }

    public List<LocalDate> getAppointmentsByMonth(Long therapistId, LocalDate date) {
        List<LocalDate> appointments = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT DISTINCT start_time FROM AVAILABILITY " +
                    "WHERE therapist_id = ? AND START_TIME >= CURRENT_DATE() AND MONTH(start_time) = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.setInt(2, Date.valueOf(date).toLocalDate().getMonth().getValue());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Timestamp startTime = resultSet.getTimestamp("start_time");
                appointments.add(startTime.toLocalDateTime().toLocalDate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public void deleteAppointment(Long appointmentId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM appointments WHERE appointment_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, appointmentId);
            int rowsDeleted = statement.executeUpdate();
            System.out.println(rowsDeleted + " rows deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
