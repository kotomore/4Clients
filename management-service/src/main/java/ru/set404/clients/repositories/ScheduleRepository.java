package ru.set404.clients.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.Schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends MongoRepository<Schedule, String> {
    List<Schedule> findByAgentIdAndDateGreaterThanEqual(String agentId, LocalDate date);
    void deleteByAgentIdAndDate(String agentId, LocalDate date);
    Optional<Schedule> findByAgentIdAndDate(String agentId, LocalDate date);
}
