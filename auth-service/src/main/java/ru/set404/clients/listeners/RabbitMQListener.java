package ru.set404.clients.listeners;

import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.set404.clients.dto.security.JwtRequest;
import ru.set404.clients.dto.security.JwtResponse;
import ru.set404.clients.dto.security.RefreshJwtRequest;
import ru.set404.clients.services.AuthService;

@EnableRabbit
@Slf4j
@RequiredArgsConstructor
@Component
public class RabbitMQListener {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @RabbitListener(queues = "login", returnExceptions = "true")
    public JwtResponse login(JwtRequest authRequest) throws AuthException {
        log.info("Try login : " + authRequest.getLogin());
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(authRequest.getLogin(), authRequest.getPassword());
        authenticationManager.authenticate(authInputToken);
        return authService.login(authRequest);
    }

    @RabbitListener(queues = "refresh")
    public JwtResponse refresh_token(RefreshJwtRequest request) throws AuthException {
        log.info("refresh token");
        if (request.refreshToken == null) throw new AuthException("Invalid token");
        return authService.refresh(request.getRefreshToken());
    }

    @RabbitListener(queues = "access")
    public JwtResponse getNewAccessToken(@Valid @RequestBody RefreshJwtRequest request) throws AuthException {
        log.info("Get access token");
        if (request.refreshToken == null) throw new AuthException("Invalid token");
        return authService.getAccessToken(request.getRefreshToken());
    }
}