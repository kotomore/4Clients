package ru.set404.clients.repositories;

import org.springframework.stereotype.Repository;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TherapistsRepositorySQL {
    private static final String DB_URL = "jdbc:h2:file:/Users/kot/Java/clients/src/main/resources/static/sampledata";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "password";

    public void createTherapist(String name, String phone, String password, String role) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO therapists (name, phone, password, role) " +
                    "VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.setString(2, phone);
            statement.setString(3, password);
            statement.setString(4, role);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Client getClientByPhoneNumber(String phoneNumber) {
        Client client = null;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM clients WHERE phone = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, phoneNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Long clientId = resultSet.getLong("client_id");
                String name = resultSet.getString("name");
                String phone = resultSet.getString("phone");
                client = new Client(clientId, name, phone);
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
                }
                else {
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
            String sql = "SELECT * FROM appointments WHERE therapist_id = ? AND start_time BETWEEN ? AND ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, appointment.getTherapistId());
            statement.setTimestamp(2, appointment.getStartTime());
            statement.setTimestamp(3, appointment.getEndTime());
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

        Client client = getClientByPhoneNumber(appointment.getClient().getPhone());

        if (client == null) {
            appointment.setClient(addClient(appointment.getClient()));
        }
        else {
            appointment.setClient(client);
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO appointments (client_id, therapist_id, service_id, start_time, end_time) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, appointment.getClient().getId());
            statement.setLong(2, appointment.getTherapistId());
            statement.setLong(3, appointment.getServiceId());
            statement.setTimestamp(4, appointment.getStartTime());
            statement.setTimestamp(5, appointment.getEndTime());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    appointment.setAppointmentId(generatedKeys.getLong(1));
                }
                else {
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
                    "WHERE therapist_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long appointmentId = resultSet.getLong("appointment_id");
                Long clientId = resultSet.getLong("client_id");
                Long serviceId = resultSet.getLong("service_id");
                Timestamp startTime = resultSet.getTimestamp("start_time");
                Timestamp endTime = resultSet.getTimestamp("end_time");
                String clientName = resultSet.getString("name");
                String clientPhone = resultSet.getString("phone");
                Client client = new Client(clientId, clientName, clientPhone);
                Appointment appointment = new Appointment(appointmentId, startTime, endTime, serviceId, therapistId, client);
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
                    "WHERE therapist_id = ? AND APPOINTMENT_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.setLong(2, appointmentId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Long clientId = resultSet.getLong("client_id");
                Long serviceId = resultSet.getLong("service_id");
                Timestamp startTime = resultSet.getTimestamp("start_time");
                Timestamp endTime = resultSet.getTimestamp("end_time");
                String clientName = resultSet.getString("name");
                String clientPhone = resultSet.getString("phone");
                Client client = new Client(clientId, clientName, clientPhone);
                appointment = new Appointment(appointmentId, startTime, endTime, serviceId, therapistId, client);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (appointment != null)
            return Optional.of(appointment);
        else return Optional.empty();
    }

    public void deleteAppointment(int appointmentId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM appointments WHERE appointment_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, appointmentId);
            int rowsDeleted = statement.executeUpdate();
            System.out.println(rowsDeleted + " rows deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
