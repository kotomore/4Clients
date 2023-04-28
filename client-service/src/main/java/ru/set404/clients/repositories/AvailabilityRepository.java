package ru.set404.clients.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.Availability;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository extends MongoRepository<Availability, String> {
    List<Availability> findByAgentIdAndDate(String agentId, LocalDate date);
    List<Availability> findByAgentIdAndDateAfter(String agentId, LocalDate date);
    Optional<Availability> findByAgentIdAndDateAndStartTime(String agentId, LocalDate date, LocalTime timeStart);
    void deleteByAgentIdAndDateAndStartTime(String agentId, LocalDate date, LocalTime timeStart);
    boolean existsByAgentIdAndDateAndStartTime(String agentId, LocalDate date, LocalTime timeStart);

}
