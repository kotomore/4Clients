package ru.set404.clients.controllers;

import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.set404.clients.dto.AgentDTO;
import ru.set404.clients.dto.security.JwtRequest;
import ru.set404.clients.dto.security.JwtResponse;
import ru.set404.clients.dto.security.RefreshJwtRequest;
import ru.set404.clients.exceptions.UserAlreadyExistException;
import ru.set404.clients.models.Agent;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final ModelMapper modelMapper;
    private final AmqpTemplate template;

    @PostMapping("/registration")
    ResponseEntity<EntityModel<Agent>> registration(@Valid @RequestBody AgentDTO agentDTO) {
        Agent savedAgent = modelMapper.map(agentDTO, Agent.class);
        Agent agent = (Agent) template.convertSendAndReceive("register", savedAgent);
        if (agent == null || agent.getPhone() == null) throw new UserAlreadyExistException();
        return ResponseEntity
                .ok().body(EntityModel.of(agent));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> performLogin(@Valid @RequestBody JwtRequest authRequest) throws AuthException {
        JwtResponse token = (JwtResponse) template.convertSendAndReceive("login", authRequest);
        if (token != null && token.getAccessToken() == null) throw new AuthException("Invalid credentials");
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> getNewRefreshToken(@Valid @RequestBody RefreshJwtRequest request) throws AuthException {
        final JwtResponse token = (JwtResponse) template.convertSendAndReceive("refresh", request);
        if (token != null && token.getAccessToken() == null) throw new AuthException("Invalid token");
        return ResponseEntity.ok(token);
    }

    @PostMapping("/token")
    public ResponseEntity<JwtResponse> getNewAccessToken(@Valid @RequestBody RefreshJwtRequest request) throws AuthException {
        final JwtResponse token = (JwtResponse) template.convertSendAndReceive("access", request);
        if (token != null && token.getAccessToken() == null) throw new AuthException("Invalid token");
        return ResponseEntity.ok(token);
    }
}
