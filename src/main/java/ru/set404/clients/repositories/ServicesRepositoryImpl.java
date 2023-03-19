package ru.set404.clients.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.set404.clients.models.Service;

import java.sql.*;
import java.util.Optional;

@Repository
public class ServicesRepositoryImpl implements ServicesRepository {
    @Value("${db.url}")
    private String DB_URL;
    @Value("${db.user}")
    private String DB_USER;
    @Value("${db.password}")
    private String DB_PASSWORD;

    @Override
    public Optional<Service> findServiceByTherapist(Long therapistId) {
        Optional<Service> service = Optional.empty();
        String sql = "SELECT * FROM SERVICES WHERE THERAPIST_ID = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);) {

            statement.setLong(1, therapistId);

            try (ResultSet resultSet = statement.executeQuery();){
                if (resultSet.next()) {
                    service = Optional.of(new Service());
                    service.get().setServiceId(resultSet.getLong("service_id"));
                    service.get().setName(resultSet.getString("name"));
                    service.get().setDescription(resultSet.getString("description"));
                    service.get().setDuration(resultSet.getInt("duration"));
                    service.get().setPrice(resultSet.getInt("price"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return service;
    }

    @Override
    public void addOrUpdateService(Long therapistId, Service service) {
        String sql;
        if (findServiceByTherapist(therapistId).isEmpty()) {
            sql = "INSERT INTO SERVICES (NAME, DESCRIPTION, DURATION, PRICE, THERAPIST_ID) " +
                    "VALUES (?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE SERVICES SET NAME = ?, DESCRIPTION = ?, DURATION = ?, PRICE = ? " +
                    "WHERE THERAPIST_ID = ?";

        }
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);) {

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
