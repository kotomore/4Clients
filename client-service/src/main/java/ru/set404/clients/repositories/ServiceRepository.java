package ru.set404.clients.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.AgentService;

import java.util.Optional;

public interface ServiceRepository extends MongoRepository<AgentService, String> {
    Optional<AgentService> findByAgentId(String agentId);
}
