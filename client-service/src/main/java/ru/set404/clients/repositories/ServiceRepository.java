package ru.set404.clients.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.AgentService;

public interface ServiceRepository extends MongoRepository<AgentService, String> {
}
