package ru.kotomore.authservice.services;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.kotomore.authservice.dto.security.JwtRequest;
import ru.kotomore.authservice.dto.security.JwtResponse;
import ru.kotomore.authservice.models.Agent;
import ru.kotomore.authservice.security.AgentDetails;
import ru.kotomore.authservice.security.JwtProvider;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AgentDetailsService service;
    private final Map<String, String> refreshStorage = new HashMap<>();
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    public JwtResponse login(@NonNull JwtRequest authRequest) {
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(authRequest.getLogin(),
                        authRequest.getPassword());
        try {
            authenticationManager.authenticate(authInputToken);
        } catch (BadCredentialsException e) {
            return new JwtResponse();
        }

        final Agent agent = ((AgentDetails) service.loadUserByUsername(authRequest.getLogin())).getAgent();
        final String accessToken = jwtProvider.generateAccessToken(agent);
        final String refreshToken = jwtProvider.generateRefreshToken(agent);
        refreshStorage.put(agent.getPhone(), refreshToken);
        return new JwtResponse(accessToken, refreshToken);
    }

    public JwtResponse refresh(@NonNull String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final Agent agent = ((AgentDetails) service.loadUserByUsername(login)).getAgent();
                final String accessToken = jwtProvider.generateAccessToken(agent);
                final String newRefreshToken = jwtProvider.generateRefreshToken(agent);
                refreshStorage.put(agent.getPhone(), newRefreshToken);
                return new JwtResponse(accessToken, newRefreshToken);
            }
        }
        return new JwtResponse();
    }

    public JwtResponse getAccessToken(@NonNull String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final Agent agent = ((AgentDetails) service.loadUserByUsername(login)).getAgent();
                final String accessToken = jwtProvider.generateAccessToken(agent);
                return new JwtResponse(accessToken, null);
            }
        }
        return new JwtResponse();
    }
}
