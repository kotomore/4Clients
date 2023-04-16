package ru.set404.clients.repositories;

import ru.set404.clients.models.Therapist;

import java.util.Optional;

public interface TherapistsRepository {
    Optional<Therapist> findTherapistByPhone(String phone);
}
