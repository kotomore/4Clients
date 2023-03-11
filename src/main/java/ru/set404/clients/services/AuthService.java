package ru.set404.clients.services;

import io.jsonwebtoken.Claims;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.set404.clients.dto.securitydto.JwtRequest;
import ru.set404.clients.dto.securitydto.JwtResponse;
import ru.set404.clients.models.Therapist;
import ru.set404.clients.security.JwtProvider;
import ru.set404.clients.security.TherapistDetails;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final TherapistDetailsService service;
    private final Map<String, String> refreshStorage = new HashMap<>();
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;


    public JwtResponse login(@NonNull JwtRequest authRequest) throws AuthException {
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(authRequest.getLogin(),
                        authRequest.getPassword());
        try {
            authenticationManager.authenticate(authInputToken);
        } catch (BadCredentialsException e) {
            throw new AuthException(e.getCause());
        }

        final Therapist therapist = ((TherapistDetails) service.loadUserByUsername(authRequest.getLogin())).getTherapist();
        final String accessToken = jwtProvider.generateAccessToken(therapist);
        final String refreshToken = jwtProvider.generateRefreshToken(therapist);
        refreshStorage.put(therapist.getPhone(), refreshToken);
        return new JwtResponse(accessToken, refreshToken);
    }

    public JwtResponse getAccessToken(@NonNull String refreshToken) throws AuthException {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final Therapist therapist = ((TherapistDetails) service.loadUserByUsername(login)).getTherapist();
                final String accessToken = jwtProvider.generateAccessToken(therapist);
                return new JwtResponse(accessToken, null);
            }
        }
        return new JwtResponse(null, null);
    }

    public JwtResponse refresh(@NonNull String refreshToken) throws AuthException {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final Therapist therapist = ((TherapistDetails) service.loadUserByUsername(login)).getTherapist();
                final String accessToken = jwtProvider.generateAccessToken(therapist);
                final String newRefreshToken = jwtProvider.generateRefreshToken(therapist);
                refreshStorage.put(therapist.getPhone(), newRefreshToken);
                return new JwtResponse(accessToken, newRefreshToken);
            }
        }
        throw new AuthException("Invalid JWT token");
    }
}
