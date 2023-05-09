package ru.kotomore.taptimes.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kotomore.taptimes.models.Availability;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository extends MongoRepository<Availability, String> {
    List<Availability> findByAgentIdAndStartTimeAfter(String agentId, LocalDateTime startTime);
    List<Availability> findByAgentIdAndStartTimeBetween(String agentId, LocalDateTime startTime, LocalDateTime endTime);
    void deleteByAgentIdAndStartTime(String agentId, LocalDateTime startTime);
    Optional<Availability> findByAgentIdAndStartTime(String agentId, LocalDateTime startTime);
    boolean existsByAgentIdAndStartTime(String agentId, LocalDateTime startTime);
}
