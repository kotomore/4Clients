package ru.set404.clients.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.Availability;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository extends MongoRepository<Availability, String> {
    Optional<Availability> findByAgentIdAndStartTime(String agentId, LocalDateTime startTime);
    List<Availability> findByAgentIdAndStartTimeBetween(String agentId, LocalDateTime startTime, LocalDateTime endTime);
    List<Availability> findByAgentIdAndStartTimeBetween(String agentId, LocalDateTime startTime, LocalDateTime endTime, Sort sort);
    void deleteByAgentIdAndStartTimeBetween(String agentId, LocalDateTime startTime, LocalDateTime endTime);
    void deleteAllByAgentId(String agentId);
}
