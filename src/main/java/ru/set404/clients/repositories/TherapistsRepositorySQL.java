package ru.set404.clients.repositories;

import org.springframework.stereotype.Repository;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Client;
import ru.set404.clients.models.Service;
import ru.set404.clients.models.Therapist;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TherapistsRepositorySQL {
    private static final String DB_URL = "jdbc:h2:file:/Users/kot/Java/clients/src/main/resources/static/sampledata";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "password";

    public Long createTherapist(Therapist therapist) {
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
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating client failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
                    "WHERE AVAILABILITY.therapist_id = ? AND AVAILABLE_DATE = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.setDate(2, Date.valueOf(date));
            ResultSet resultSet = statement.executeQuery();

            List<LocalTime> appointedTime = getAppointmentsByDay(therapistId, date);

            if (resultSet.next()) {
                LocalTime startTime = resultSet.getTime("start_time").toLocalTime();
                LocalTime endTime = resultSet.getTime("end_time").toLocalTime();
                int duration = resultSet.getInt("duration");
                for (LocalTime time = startTime; time.isBefore(endTime); time = time.plusMinutes(duration)) {
                    if (!appointedTime.contains(time)) {
                        availableTimes.add(time);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableTimes;
    }

    public List<LocalDate> getAvailableDates(Long therapistId, LocalDate date) {
        List<LocalDate> availableDates = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT AVAILABLE_DATE FROM AVAILABILITY " +
                    "WHERE therapist_id = ? AND AVAILABLE_DATE >= CURRENT_DATE() AND MONTH(AVAILABLE_DATE) = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.setInt(2, Date.valueOf(date).toLocalDate().getMonth().getValue());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                LocalDate availableDate = resultSet.getDate("available_date").toLocalDate();
                availableDates.add(availableDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableDates;
    }

    public void deleteAppointment(Long appointmentId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM appointments WHERE appointment_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, appointmentId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Therapist> getAllTherapist() {
        List<Therapist> therapists = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM THERAPISTS";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Therapist therapist = new Therapist();
                therapist.setId(resultSet.getLong("therapist_id"));
                therapist.setName(resultSet.getString("name"));
                therapist.setPassword(resultSet.getString("password"));
                therapist.setPhone(resultSet.getString("phone"));
                therapist.setRole(resultSet.getString("role"));
                therapists.add(therapist);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return therapists;
    }

    public Optional<Therapist> getTherapist(Long therapistId) {
        Optional<Therapist> therapist = Optional.empty();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM THERAPISTS WHERE THERAPIST_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                therapist = Optional.of(new Therapist());
                therapist.get().setId(resultSet.getLong("therapist_id"));
                therapist.get().setName(resultSet.getString("name"));
                therapist.get().setPassword(resultSet.getString("password"));
                therapist.get().setPhone(resultSet.getString("phone"));
                therapist.get().setRole(resultSet.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return therapist;
    }

    public void updateTherapist(Therapist therapist) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE THERAPISTS SET NAME = ?, PASSWORD = ?, PHONE = ?, ROLE = ? WHERE THERAPIST_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, therapist.getName());
            statement.setString(2, therapist.getPassword());
            statement.setString(3, therapist.getPhone());
            statement.setString(4, therapist.getRole());
            statement.setLong(5, therapist.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isHaveAvailableTime(Long therapistId, LocalDate date) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM AVAILABILITY WHERE THERAPIST_ID = ? AND AVAILABLE_DATE = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.setDate(2, Date.valueOf(date));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void addorUpdateAvailableTime(Long therapistId, LocalDate date, LocalTime timeStart, LocalTime timeEnd) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            String sql;
            if (isHaveAvailableTime(therapistId, date)) {
                sql = "INSERT INTO AVAILABILITY (START_TIME, END_TIME, THERAPIST_ID, AVAILABLE_DATE) " +
                        "VALUES (?, ?, ?, ?)";
            } else {
                sql = "UPDATE AVAILABILITY SET START_TIME = ?, END_TIME = ? WHERE THERAPIST_ID = ? AND AVAILABLE_DATE = ?";

            }
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setTime(1, Time.valueOf(timeStart));
            statement.setTime(2, Time.valueOf(timeEnd));
            statement.setLong(3, therapistId);
            statement.setDate(4, Date.valueOf(date));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAvailableTime(Long therapistId, LocalDate date) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM AVAILABILITY WHERE THERAPIST_ID = ? and AVAILABLE_DATE = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.setDate(1, Date.valueOf(date));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTherapist(Long therapistId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM THERAPISTS WHERE THERAPIST_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<Service> getServiceByTherapist(Long therapistId) {
        Optional<Service> service = Optional.empty();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM SERVICES WHERE THERAPIST_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                service = Optional.of(new Service());
                service.get().setServiceId(resultSet.getLong("service_id"));
                service.get().setName(resultSet.getString("name"));
                service.get().setDescription(resultSet.getString("description"));
                service.get().setDuration(resultSet.getInt("duration"));
                service.get().setPrice(resultSet.getInt("price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return service;
    }

    public void addOrUpdateService(Long therapistId, Service service) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql;
            if (getServiceByTherapist(therapistId).isEmpty()) {
                sql = "INSERT INTO SERVICES (NAME, DESCRIPTION, DURATION, PRICE, THERAPIST_ID) " +
                        "VALUES (?, ?, ?, ?, ?)";
            } else {
                sql = "UPDATE SERVICES SET NAME = ?, DESCRIPTION = ?, DURATION = ?, PRICE = ? " +
                        "WHERE THERAPIST_ID = ?";

            }
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, service.getName());
            statement.setString(2, service.getDescription());
            statement.setInt(3, service.getDuration());
            statement.setInt(4, service.getPrice());
            statement.setLong(5, therapistId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
