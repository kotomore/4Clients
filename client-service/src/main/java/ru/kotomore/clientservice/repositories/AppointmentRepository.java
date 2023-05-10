package ru.kotomore.clientservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kotomore.clientservice.models.Appointment;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
}
