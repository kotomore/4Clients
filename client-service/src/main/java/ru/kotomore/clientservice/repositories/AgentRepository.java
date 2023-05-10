package ru.kotomore.clientservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kotomore.clientservice.models.Agent;

public interface AgentRepository extends MongoRepository<Agent, String> {
}
