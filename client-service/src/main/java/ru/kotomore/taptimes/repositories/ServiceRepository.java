package ru.kotomore.taptimes.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kotomore.taptimes.models.AgentService;

import java.util.Optional;

public interface ServiceRepository extends MongoRepository<AgentService, String> {
    Optional<AgentService> findByAgentId(String agentId);
}
