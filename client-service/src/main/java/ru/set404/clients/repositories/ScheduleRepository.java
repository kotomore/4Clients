package ru.set404.clients.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends MongoRepository<Schedule, String> {
    List<Schedule> findByAgentIdAndDateGreaterThanEqual(String agentId, LocalDate date);
    List<Schedule> findByAgentIdAndDate(String agentId, LocalDate date);
}
