package ru.set404.clients.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.set404.clients.models.Agent;
import ru.set404.clients.repositories.AgentRepository;
import ru.set404.clients.security.AgentDetails;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentDetailsService implements UserDetailsService {

    private final AgentRepository agentRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<Agent> agent = agentRepository.findTherapistByPhone(s);

        if (agent.isEmpty())
            throw new UsernameNotFoundException(String.format("User with phone - %s not found", s));

        return new AgentDetails(agent.get());
    }
}
