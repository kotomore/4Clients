package ru.set404.clients.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.Agent;

public interface AgentRepository extends MongoRepository<Agent, String> {
}
