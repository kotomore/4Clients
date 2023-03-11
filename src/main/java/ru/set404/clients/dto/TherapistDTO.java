package ru.set404.clients.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.set404.clients.models.Appointment;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TherapistDTO {
    @NotEmpty(message = "Not be empty")
    private String name;
    @NotEmpty(message = "Not be empty")
    private String phone;
    @NotEmpty(message = "Not be empty")
    private String password;
}
