package ru.set404.clients.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.set404.clients.models.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ClientsRepositoryImpl implements ClientsRepository {

    @Value("${db.url}")
    private String DB_URL;
    @Value("${db.user}")
    private String DB_USER;
    @Value("${db.password}")
    private String DB_PASSWORD;

    @Override
    public Optional<Client> findClientByPhoneNumber(String phoneNumber) {
        Optional<Client> client = Optional.empty();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM clients WHERE phone = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, phoneNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                client = Optional.of(makeClientFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return client;
    }

    @Override
    public Client createClient(Client client) {
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

    @Override
    public List<Client> findClientsForTherapist(Long therapistId) {
        List<Client> clients = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT C.CLIENT_ID, C.NAME, C.PHONE FROM appointments " +
                    "JOIN CLIENTS C on C.CLIENT_ID = APPOINTMENTS.CLIENT_ID " +
                    "WHERE APPOINTMENTS.therapist_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, therapistId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                clients.add(makeClientFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    private Client makeClientFromResultSet(ResultSet resultSet) throws SQLException {
        Long clientId = resultSet.getLong("client_id");
        String clientName = resultSet.getString("name");
        String clientPhone = resultSet.getString("phone");
        return new Client(clientId, clientName, clientPhone);
    }
}
