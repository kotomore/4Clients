package ru.kotomore.authservice.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentDTO {
    @NotEmpty(message = "Not be empty")
    @Size(min = 3, max = 20, message = "Name size must be between 3 and 20 characters")
    private String name;
    @NotEmpty(message = "Not be empty")
    @Size(min = 3, max = 20, message = "Phone size must be between 3 and 20 characters")
    private String phone;
    @NotEmpty(message = "Not be empty")
    @Size(min = 3, max = 20, message = "Password size must be between 3 and 20 characters")
    private String password;
}
