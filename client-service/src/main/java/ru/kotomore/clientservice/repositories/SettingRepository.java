package ru.kotomore.clientservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kotomore.clientservice.models.AgentSettings;

import java.util.Optional;

public interface SettingRepository extends MongoRepository<AgentSettings, String> {
    Optional<AgentSettings> findByVanityUrl(String vanityUrl);
}
