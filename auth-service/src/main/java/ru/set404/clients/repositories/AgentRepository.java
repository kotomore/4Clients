package ru.set404.clients.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.Agent;

import java.util.Optional;

public interface AgentRepository extends MongoRepository<Agent, String> {
    Optional<Agent> findTherapistByPhone(String phone);
}
