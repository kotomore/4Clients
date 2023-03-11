package ru.set404.clients.controllers;

import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.dto.TherapistDTO;
import ru.set404.clients.dto.securitydto.JwtRequest;
import ru.set404.clients.dto.securitydto.JwtResponse;
import ru.set404.clients.dto.securitydto.RefreshJwtRequest;
import ru.set404.clients.models.Therapist;
import ru.set404.clients.services.AuthService;
import ru.set404.clients.services.RegistrationService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;


    @PostMapping("/registration")
    @CrossOrigin
    ResponseEntity<EntityModel<TherapistDTO>> newTherapist(@RequestBody TherapistDTO therapist) {
        registrationService.saveTherapist(modelMapper.map(therapist, Therapist.class));
        return ResponseEntity
                .created(linkTo(methodOn(TherapistController.class).getTherapistById()).toUri()).build();
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> performLogin(@RequestBody JwtRequest authRequest) throws AuthException {
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(authRequest.getLogin(), authRequest.getPassword());

        try {
            authenticationManager.authenticate(authInputToken);
        } catch (BadCredentialsException e) {
            throw new AuthException("Invalid credentials");
        }

        final JwtResponse token = authService.login(authRequest);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/token")
    public ResponseEntity<JwtResponse> getNewAccessToken(@RequestBody RefreshJwtRequest request) throws AuthException {
        final JwtResponse token = authService.getAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> getNewRefreshToken(@RequestBody RefreshJwtRequest request) throws AuthException {
        final JwtResponse token = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(token);
    }

}
