package ru.kotomore.managementservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kotomore.managementservice.models.AgentSettings;

import java.util.Optional;

public interface SettingRepository extends MongoRepository<AgentSettings, String> {
    Optional<AgentSettings> findByAgentId(String agentId);
    boolean existsByVanityUrl(String vanityUrl);
}
