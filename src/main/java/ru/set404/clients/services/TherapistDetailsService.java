package ru.set404.clients.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.set404.clients.models.Therapist;
import ru.set404.clients.repositories.TherapistsRepository;
import ru.set404.clients.security.TherapistDetails;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TherapistDetailsService implements UserDetailsService {

    private final TherapistsRepository therapistsRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<Therapist> therapist = therapistsRepository.findTherapistByPhone(s);

        if (therapist.isEmpty())
            throw new UsernameNotFoundException(String.format("User with phone - %s not found", s));

        return new TherapistDetails(therapist.get());
    }
}
