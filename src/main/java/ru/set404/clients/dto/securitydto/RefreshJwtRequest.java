package ru.set404.clients.dto.securitydto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshJwtRequest {
    @NotEmpty(message = "Not be empty")
    public String refreshToken;

}