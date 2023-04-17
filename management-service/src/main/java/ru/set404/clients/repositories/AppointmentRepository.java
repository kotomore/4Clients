package ru.set404.clients.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.Appointment;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByAgentId(String agentId);
    Optional<Appointment> findByIdAndAgentId(String appointmentId, String agentId);
    void deleteByIdAndAgentId(String appointmentId, String agentId);
}
