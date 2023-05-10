package ru.kotomore.clientservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kotomore.clientservice.models.AgentService;

import java.util.Optional;

public interface ServiceRepository extends MongoRepository<AgentService, String> {
    Optional<AgentService> findByAgentId(String agentId);
}
