package ru.set404.clients.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.set404.clients.models.Role;
import ru.set404.clients.models.Therapist;

import java.sql.*;
import java.util.Optional;

@Repository
public class TherapistsRepositoryImpl implements TherapistsRepository {
    @Value("${db.url}")
    private String DB_URL;
    @Value("${db.user}")
    private String DB_USER;
    @Value("${db.password}")
    private String DB_PASSWORD;

    @Override
    public Long createTherapist(Therapist therapist) {
        String sql = "INSERT INTO therapists (name, phone, password, role) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

            statement.setString(1, therapist.getName());
            statement.setString(2, therapist.getPhone());
            statement.setString(3, therapist.getPassword());
            statement.setString(4, therapist.getRole().getValue());
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

    @Override
    public Optional<Therapist> findTherapistById(Long therapistId) {
        Optional<Therapist> therapist = Optional.empty();
        String sql = "SELECT * FROM THERAPISTS WHERE THERAPIST_ID = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);) {

            statement.setLong(1, therapistId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    therapist = Optional.of(makeTherapistFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return therapist;
    }

    @Override
    public Optional<Therapist> findTherapistByPhone(String phone) {
        Optional<Therapist> therapist = Optional.empty();
        String sql = "SELECT * FROM THERAPISTS WHERE PHONE = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, phone);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    therapist = Optional.of(makeTherapistFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return therapist;
    }

    @Override
    public void updateTherapist(Therapist therapist) {
        String sql = "UPDATE THERAPISTS SET NAME = ?, PASSWORD = ?, PHONE = ?, ROLE = ? WHERE THERAPIST_ID = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);) {

            statement.setString(1, therapist.getName());
            statement.setString(2, therapist.getPassword());
            statement.setString(3, therapist.getPhone());
            statement.setString(4, therapist.getRole().getValue());
            statement.setLong(5, therapist.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTherapist(Long therapistId) {
        String sql = "DELETE FROM THERAPISTS WHERE THERAPIST_ID = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);) {

            statement.setLong(1, therapistId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Therapist makeTherapistFromResultSet(ResultSet resultSet) throws SQLException {
        Therapist therapist = new Therapist();
        therapist.setId(resultSet.getLong("therapist_id"));
        therapist.setName(resultSet.getString("name"));
        therapist.setPassword(resultSet.getString("password"));
        therapist.setPhone(resultSet.getString("phone"));
        therapist.setRole(Role.valueOf(resultSet.getString("role")));
        return therapist;
    }
}
