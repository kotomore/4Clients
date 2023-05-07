package ru.kotomore.taptimes.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.kotomore.taptimes.models.Agent;
import ru.kotomore.taptimes.repositories.AgentRepository;
import ru.kotomore.taptimes.security.AgentDetails;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentDetailsService implements UserDetailsService {

    private final AgentRepository agentRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<Agent> agent = agentRepository.findAgentByPhone(s);

        if (agent.isEmpty())
            throw new UsernameNotFoundException(String.format("User with phone - %s not found", s));

        return new AgentDetails(agent.get());
    }
}
