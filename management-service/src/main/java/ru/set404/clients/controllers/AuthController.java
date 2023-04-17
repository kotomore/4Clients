package ru.set404.clients.controllers;

import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.dto.AgentDTO;
import ru.set404.clients.dto.security.JwtRequest;
import ru.set404.clients.dto.security.JwtResponse;
import ru.set404.clients.dto.security.RefreshJwtRequest;
import ru.set404.clients.services.RegistrationService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final RegistrationService registrationService;
    private final AmqpTemplate template;

    @PostMapping("/registration")
    ResponseEntity<EntityModel<AgentDTO>> newTherapist(@RequestBody AgentDTO agentDTO) {
        registrationService.saveAgent(agentDTO);
        return ResponseEntity
                .created(linkTo(methodOn(ManagementController.class).getCurrentTherapist()).toUri()).build();
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
