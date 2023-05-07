package ru.kotomore.taptimes.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kotomore.taptimes.models.Appointment;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
}
