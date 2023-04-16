package ru.set404.clients.repositories;

import ru.set404.clients.models.Therapist;

import java.util.Optional;

public interface TherapistsRepository {
    Long createTherapist(Therapist therapist);

    Optional<Therapist> findTherapistById(Long therapistId);

    Optional<Therapist> findTherapistByPhone(String phone);

    void updateTherapist(Therapist therapist);

    void deleteTherapist(Long therapistId);
}
