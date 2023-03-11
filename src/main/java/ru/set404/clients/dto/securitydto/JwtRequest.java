package ru.set404.clients.dto.securitydto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtRequest {

    @NotEmpty(message = "Not be empty")
    @Size(min = 3, max = 20, message = "Login size must be between 3 and 10 characters")
    private String login;
    @NotEmpty(message = "Not be empty")
    @Size(min = 3, max = 20, message = "Password size must be between 3 and 10 characters")
    private String password;
}
