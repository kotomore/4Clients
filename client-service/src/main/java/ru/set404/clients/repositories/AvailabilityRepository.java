package ru.set404.clients.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.Availability;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AvailabilityRepository extends MongoRepository<Availability, String> {
    List<Availability> findByAgentIdAndDateBetween(String agentId, LocalDate dateStart, LocalDate dateEnd);
    List<Availability> findByAgentIdAndDateAfter(String agentId, LocalDate date);
    void deleteByAgentIdAndDateAndStartTime(String agentId, LocalDate date, LocalTime timeStart);
    boolean existsByAgentIdAndDateAndStartTime(String agentId, LocalDate date, LocalTime timeStart);
}
