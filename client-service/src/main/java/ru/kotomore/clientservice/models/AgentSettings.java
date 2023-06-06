package ru.kotomore.clientservice.models;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AgentSettings {
    @Id
    private String id;
    private String agentId;
    private String vanityUrl;
}
