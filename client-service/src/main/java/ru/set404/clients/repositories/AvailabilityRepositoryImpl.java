package ru.set404.clients.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.set404.clients.models.Appointment;

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
        String sql = "SELECT * FROM appointments WHERE therapist_id = ? AND start_time = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, appointment.getTherapistId());
            statement.setTimestamp(2, Timestamp.valueOf(appointment.getStartTime()));

            try (ResultSet resultSet = statement.executeQuery();){
                if (resultSet.next()) {
                    isAvailable = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            isAvailable = false;
        }
        return isAvailable;
    }

    @Override
    public List<LocalTime> findAvailableTimes(Long therapistId, LocalDate date, List<LocalTime> appointedTime) {
        List<LocalTime> availableTimes = new ArrayList<>();
        String sql = "SELECT START_TIME, END_TIME, DURATION FROM AVAILABILITY " +
                "JOIN SERVICES ON AVAILABILITY.THERAPIST_ID = SERVICES.THERAPIST_ID " +
                "WHERE AVAILABILITY.therapist_id = ? AND AVAILABLE_DATE = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, therapistId);
            statement.setDate(2, Date.valueOf(date));

            try (ResultSet resultSet = statement.executeQuery();){
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableTimes;
    }

    @Override
    public List<LocalDate> findAvailableDates(Long therapistId, LocalDate date, List<LocalTime> appointedTime) {
        List<LocalDate> availableDates = new ArrayList<>();
        String sql = "SELECT AVAILABLE_DATE FROM AVAILABILITY " +
                "WHERE therapist_id = ? AND AVAILABLE_DATE >= CURRENT_DATE() AND MONTH(AVAILABLE_DATE) = ? " +
                "AND ISFULL = false";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, therapistId);
            statement.setInt(2, Date.valueOf(date).toLocalDate().getMonth().getValue());
            try (ResultSet resultSet = statement.executeQuery()){
                while (resultSet.next()) {
                    LocalDate availableDate = resultSet.getDate("available_date").toLocalDate();
                    if (availableDate.isEqual(LocalDate.now()) && findAvailableTimes(therapistId, availableDate, appointedTime).size() < 1)
                        markAvailabilityAs(therapistId, availableDate, true);
                    else
                        availableDates.add(availableDate);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableDates;
    }

    public void markAvailabilityAs(Long therapistId, LocalDate date, boolean markAs) {
        String sql = "UPDATE AVAILABILITY SET ISFULL = ? WHERE THERAPIST_ID = ? AND AVAILABLE_DATE = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);) {

            statement.setBoolean(1, markAs);
            statement.setLong(2, therapistId);
            statement.setDate(3, Date.valueOf(date));
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
