package ru.kotomore.authservice.dto.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JwtResponse implements Serializable {
    private final String type = "Bearer";
    private String accessToken;
    private String refreshToken;
}
