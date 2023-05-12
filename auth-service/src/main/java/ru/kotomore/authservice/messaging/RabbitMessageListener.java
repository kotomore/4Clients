package ru.kotomore.authservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.kotomore.authservice.dto.security.JwtRequest;
import ru.kotomore.authservice.dto.security.JwtResponse;
import ru.kotomore.authservice.dto.security.RefreshJwtRequest;
import ru.kotomore.authservice.models.Agent;
import ru.kotomore.authservice.services.AuthService;
import ru.kotomore.authservice.services.RegistrationService;

@EnableRabbit
@Slf4j
@RequiredArgsConstructor
@Component
public class RabbitMessageListener {

    private final AuthService authService;
    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;

    @RabbitListener(queues = "register", returnExceptions = "true")
    public Agent register(Agent agent) {
        log.info("Register new agent with phone - " + agent.getPhone());
        return registrationService.saveAgent(agent);
    }

    @RabbitListener(queues = "login", returnExceptions = "true")
    public JwtResponse login(JwtRequest authRequest) {
        log.info("Try authenticate with login - " + authRequest.getLogin());
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(authRequest.getLogin(), authRequest.getPassword());
        authenticationManager.authenticate(authInputToken);
        return authService.login(authRequest);
    }

    @RabbitListener(queues = "refresh", returnExceptions = "true")
    public JwtResponse refresh_token(RefreshJwtRequest request) {
        log.info("refresh token");
        return authService.refresh(request.getRefreshToken());
    }

    @RabbitListener(queues = "access", returnExceptions = "true")
    public JwtResponse getNewAccessToken(RefreshJwtRequest request) {
        log.info("Get access token");
        return authService.getAccessToken(request.getRefreshToken());
    }
}