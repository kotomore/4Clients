package ru.set404.clients.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.set404.clients.dto.AvailabilitiesDTO;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Availability;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AvailabilityRepositoryImpl implements AvailabilityRepository {

    @Value("${db.url}")
    private String DB_URL;
    @Value("${db.user}")
    private String DB_USER;
    @Value("${db.password}")
    private String DB_PASSWORD;

    @Override
    public boolean isTimeAvailable(Appointment appointment) {
        boolean isAvailable = true;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM appointments WHERE therapist_id = ? AND start_time = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, appointment.getTherapistId());
            statement.setTimestamp(2, Timestamp.valueOf(appointment.getStartTime()));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                isAvailable = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isAvailable;
    }

    @Override
    public List<LocalTime> findAvailableTimes(Long therapistId, LocalDate date, List<LocalTime> appointedTime) {
        List<LocalTime> availableTimes = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT START_TIME, END_TIME, DURATION FROM AVAILABILITY " +
                    "JOIN SERVICES ON AVAILABILITY.THERAPIST_ID = SERVICES.THERAPIST_ID " +
                    "WHERE AVAILABILITY.therapist_id = ? AND AVAILABLE_DATE = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.setDate(2, Date.valueOf(date));
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                LocalTime startTime = resultSet.getTime("start_time").toLocalTime();

                LocalTime endTime = resultSet.getTime("end_time").toLocalTime();
                int duration = resultSet.getInt("duration");
                for (LocalTime time = startTime; time.isBefore(endTime); time = time.plusMinutes(duration)) {
                    if (!appointedTime.contains(time) && (!LocalDate.now().isEqual(date) || time.isAfter(LocalTime.now()))) {
                        availableTimes.add(time);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableTimes;
    }

    @Override
    public List<LocalDate> findAvailableDates(Long therapistId, LocalDate date, List<LocalTime> appointedTime) {
        List<LocalDate> availableDates = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT AVAILABLE_DATE FROM AVAILABILITY " +
                    "WHERE therapist_id = ? AND AVAILABLE_DATE >= CURRENT_DATE() AND MONTH(AVAILABLE_DATE) = ? " +
                    "AND ISFULL = false";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.setInt(2, Date.valueOf(date).toLocalDate().getMonth().getValue());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                LocalDate availableDate = resultSet.getDate("available_date").toLocalDate();
                if (availableDate.isEqual(LocalDate.now()) && findAvailableTimes(therapistId, availableDate, appointedTime).size() < 1)
                    markAvailabilityAs(therapistId, availableDate, true);
                else
                    availableDates.add(availableDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableDates;
    }

    @Override
    public boolean isHaveAvailableTime(Long therapistId, LocalDate date) {
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

    @Override
    public void addOrUpdateAvailableTime(Long therapistId, Availability availability, List<LocalTime> appointedTime) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql;
            if (isHaveAvailableTime(therapistId, availability.getDate())) {
                sql = "INSERT INTO AVAILABILITY (START_TIME, END_TIME, THERAPIST_ID, AVAILABLE_DATE) " +
                        "VALUES (?, ?, ?, ?)";
            } else {
                sql = "UPDATE AVAILABILITY SET START_TIME = ?, END_TIME = ? WHERE THERAPIST_ID = ? AND AVAILABLE_DATE = ?";

            }
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setTime(1, Time.valueOf(availability.getStartTime()));
            statement.setTime(2, Time.valueOf(availability.getEndTime()));
            statement.setLong(3, therapistId);
            statement.setDate(4, Date.valueOf(availability.getDate()));
            statement.executeUpdate();

            markAvailabilityAs(therapistId, availability.getDate(), findAvailableTimes(therapistId, availability.getDate(), appointedTime).size() < 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addOrUpdateAvailableTime(Long therapistId, AvailabilitiesDTO availabilitiesDTO, List<LocalTime> appointedTime) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            for (LocalDate date = availabilitiesDTO.getStartTime().toLocalDate();
                 date.isBefore(availabilitiesDTO.getEndTime().toLocalDate());
                 date = date.plusDays(1)) {
                String sql;
                if (isHaveAvailableTime(therapistId, date)) {
                    sql = "INSERT INTO AVAILABILITY (START_TIME, END_TIME, THERAPIST_ID, AVAILABLE_DATE) " +
                            "VALUES (?, ?, ?, ?)";
                } else {
                    sql = "UPDATE AVAILABILITY SET START_TIME = ?, END_TIME = ? WHERE THERAPIST_ID = ? AND AVAILABLE_DATE = ?";

                }

                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setTime(1, Time.valueOf(availabilitiesDTO.getStartTime().toLocalTime()));
                statement.setTime(2, Time.valueOf(availabilitiesDTO.getEndTime().toLocalTime()));
                statement.setLong(3, therapistId);
                statement.setDate(4, Date.valueOf(date));
                statement.executeUpdate();

                markAvailabilityAs(therapistId, date, findAvailableTimes(therapistId, date, appointedTime).size() < 1);


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAvailableTime(Long therapistId, LocalDate date) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM AVAILABILITY WHERE THERAPIST_ID = ? and AVAILABLE_DATE = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            statement.setDate(2, Date.valueOf(date));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markAvailabilityAs(Long therapistId, LocalDate date, boolean markAs) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE AVAILABILITY SET ISFULL = ? WHERE THERAPIST_ID = ? AND AVAILABLE_DATE = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setBoolean(1, markAs);
            statement.setLong(2, therapistId);
            statement.setDate(3, Date.valueOf(date));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
