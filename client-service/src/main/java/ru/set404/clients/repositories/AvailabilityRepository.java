package ru.set404.clients.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.Availability;

import java.time.LocalDateTime;
import java.util.List;

public interface AvailabilityRepository extends MongoRepository<Availability, String> {
    List<Availability> findByAgentIdAndStartTimeAfter(String agentId, LocalDateTime startTime);
    List<Availability> findByAgentIdAndStartTimeBetween(String agentId, LocalDateTime startTime, LocalDateTime endTime);
    void deleteByAgentIdAndStartTime(String agentId, LocalDateTime startTime);
    boolean existsByAgentIdAndStartTime(String agentId, LocalDateTime startTime);
}
