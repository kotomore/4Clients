package ru.kotomore.authservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kotomore.authservice.models.Agent;

import java.util.Optional;

public interface AgentRepository extends MongoRepository<Agent, String> {
    Optional<Agent> findAgentByPhone(String phone);
}
