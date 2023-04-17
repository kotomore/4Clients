package ru.set404.clients.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ClientDTO {
    @NotEmpty(message = "Not be empty")
    private String name;
    @NotEmpty(message = "Not be empty")
    private String phone;
}
