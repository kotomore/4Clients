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
    public Optional<Therapist> findTherapistByPhone(String phone) {
        Optional<Therapist> therapist = Optional.empty();
        String sql = "SELECT * FROM THERAPISTS WHERE PHONE = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, phone);

            try (ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()) {
                    therapist = Optional.of(makeTherapistFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return therapist;
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
