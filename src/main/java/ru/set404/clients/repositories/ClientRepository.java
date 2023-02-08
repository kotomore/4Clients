package ru.set404.clients.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.set404.clients.models.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
