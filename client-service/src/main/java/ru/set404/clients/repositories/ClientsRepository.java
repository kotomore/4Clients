package ru.set404.clients.repositories;

import ru.set404.clients.models.Client;

import java.util.Optional;

public interface ClientsRepository {
    Optional<Client> findClientByPhoneNumber(String phoneNumber);

    Client createClient(Client client);
}
