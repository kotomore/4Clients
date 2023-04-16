package ru.set404.clients.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.set404.clients.models.Client;

import java.sql.*;
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
        String sql = "SELECT * FROM clients WHERE phone = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);) {

            statement.setString(1, phoneNumber);

            try (ResultSet resultSet = statement.executeQuery();){
                if (resultSet.next()) {
                    client = Optional.of(makeClientFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return client;
    }

    @Override
    public Client createClient(Client client) {
        String sql = "INSERT INTO clients (name, phone) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

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

    private Client makeClientFromResultSet(ResultSet resultSet) throws SQLException {
        Long clientId = resultSet.getLong("client_id");
        String clientName = resultSet.getString("name");
        String clientPhone = resultSet.getString("phone");
        return new Client(clientId, clientName, clientPhone);
    }
}
