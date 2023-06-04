package ru.kotomore.authservice.controllers;

import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kotomore.authservice.dto.AgentDTO;
import ru.kotomore.authservice.dto.security.JwtRequest;
import ru.kotomore.authservice.dto.security.JwtResponse;
import ru.kotomore.authservice.dto.security.RefreshJwtRequest;
import ru.kotomore.authservice.exceptions.UserAlreadyExistException;
import ru.kotomore.authservice.models.Agent;
import ru.kotomore.authservice.services.AuthService;
import ru.kotomore.authservice.services.RegistrationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final ModelMapper modelMapper;
    private final AuthService authService;
    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/registration")
    ResponseEntity<EntityModel<Agent>> registration(@Valid @RequestBody AgentDTO agentDTO) {
        Agent savedAgent = modelMapper.map(agentDTO, Agent.class);
        log.info("Register new agent with phone - " + agentDTO.getPhone());
        savedAgent = registrationService.saveAgent(savedAgent);
        if (savedAgent == null || savedAgent.getPhone() == null) throw new UserAlreadyExistException();
        return ResponseEntity
                .ok().body(EntityModel.of(savedAgent));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> performLogin(@Valid @RequestBody JwtRequest authRequest) throws AuthException {
        log.info("Try authenticate with login - " + authRequest.getLogin());
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(authRequest.getLogin(), authRequest.getPassword());
        authenticationManager.authenticate(authInputToken);
        JwtResponse token = authService.login(authRequest);
        if (token != null && token.getAccessToken() == null) throw new AuthException("Invalid credentials");
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> getNewRefreshToken(@Valid @RequestBody RefreshJwtRequest request) throws AuthException {
        log.info("refresh token");
        final JwtResponse token = authService.refresh(request.getRefreshToken());
        if (token != null && token.getAccessToken() == null) throw new AuthException("Invalid token");
        return ResponseEntity.ok(token);
    }

    @PostMapping("/token")
    public ResponseEntity<JwtResponse> getNewAccessToken(@Valid @RequestBody RefreshJwtRequest request) throws AuthException {
        log.info("Get access token");
        final JwtResponse token = authService.getAccessToken(request.getRefreshToken());
        if (token != null && token.getAccessToken() == null) throw new AuthException("Invalid token");
        return ResponseEntity.ok(token);
    }
}
