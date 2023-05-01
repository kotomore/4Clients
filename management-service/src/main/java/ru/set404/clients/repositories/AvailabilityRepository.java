package ru.set404.clients.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.Availability;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository extends MongoRepository<Availability, String> {
    List<Availability> findByAgentIdAndStartTimeAfter(String agentId, LocalDateTime startTime, Sort sort);
    Optional<Availability> findByAgentIdAndStartTime(String agentId, LocalDateTime startTime);
    List<Availability> findByAgentIdAndStartTimeBetween(String agentId, LocalDateTime startTime, LocalDateTime endTime);
    void deleteByAgentIdAndStartTimeBetween(String agentId, LocalDateTime startTime, LocalDateTime endTime);
}
