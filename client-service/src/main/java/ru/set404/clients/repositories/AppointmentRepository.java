package ru.set404.clients.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.set404.clients.models.Appointment;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
}
