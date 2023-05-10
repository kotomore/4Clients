package ru.kotomore.authservice.listeners;

import jakarta.security.auth.message.AuthException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import ru.kotomore.authservice.dto.security.JwtRequest;
import ru.kotomore.authservice.dto.security.JwtResponse;
import ru.kotomore.authservice.dto.security.RefreshJwtRequest;
import ru.kotomore.authservice.services.AuthService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RabbitMQListenerTest {

    private Validator validator;

    @Mock
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private RabbitMQListener listener;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void login_shouldReturnJwtResponseOnValidJwtRequest() throws AuthException {
        JwtRequest jwtRequest = new JwtRequest("username", "password");
        JwtResponse expectedResponse = new JwtResponse("accessToken", "refreshToken");
        when(authService.login(jwtRequest)).thenReturn(expectedResponse);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);

        JwtResponse actualResponse = listener.login(jwtRequest);

        verify(authService, times(1)).login(jwtRequest);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void refresh_token_shouldReturnJwtResponseOnValidRefreshJwtRequest() throws AuthException {
        RefreshJwtRequest refreshJwtRequest = new RefreshJwtRequest("refreshToken");
        JwtResponse expectedResponse = new JwtResponse("newAccessToken", "newRefreshToken");
        when(authService.refresh(refreshJwtRequest.getRefreshToken())).thenReturn(expectedResponse);

        JwtResponse actualResponse = listener.refresh_token(refreshJwtRequest);

        verify(authService, times(1)).refresh(refreshJwtRequest.getRefreshToken());
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void getNewAccessToken_shouldReturnJwtResponseOnValidRefreshJwtRequest() throws AuthException {
        RefreshJwtRequest refreshJwtRequest = new RefreshJwtRequest("refreshToken");
        JwtResponse expectedResponse = new JwtResponse("newAccessToken", "newRefreshToken");
        when(authService.getAccessToken(refreshJwtRequest.getRefreshToken())).thenReturn(expectedResponse);

        JwtResponse actualResponse = listener.getNewAccessToken(refreshJwtRequest);

        verify(authService, times(1)).getAccessToken(refreshJwtRequest.getRefreshToken());
        assertEquals(expectedResponse, actualResponse);
    }
}
