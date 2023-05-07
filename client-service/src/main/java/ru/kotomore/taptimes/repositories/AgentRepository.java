package ru.kotomore.taptimes.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kotomore.taptimes.models.Agent;

public interface AgentRepository extends MongoRepository<Agent, String> {
}
