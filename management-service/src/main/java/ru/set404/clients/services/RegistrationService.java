package ru.set404.clients.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.set404.clients.exceptions.UserAlreadyExistException;
import ru.set404.clients.models.Role;
import ru.set404.clients.models.Therapist;
import ru.set404.clients.repositories.TherapistsRepository;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final TherapistsRepository therapistsRepository;
    private final PasswordEncoder passwordEncoder;

    public Long saveTherapist(Therapist therapist) {
        if (therapistsRepository.findTherapistByPhone(therapist.getPhone()).isPresent())
            throw new UserAlreadyExistException();
        therapist.setPassword(passwordEncoder.encode(therapist.getPassword()));
        therapist.setRole(Role.USER);
        return therapistsRepository.createTherapist(therapist);
    }
}
