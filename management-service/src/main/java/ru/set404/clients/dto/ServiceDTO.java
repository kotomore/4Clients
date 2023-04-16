package ru.set404.clients.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceDTO {
    @NotEmpty(message = "Not be empty")
    private String name;
    @NotEmpty(message = "Not be empty")
    private String description;
    @Min(value = 1, message = "> 1")
    private int duration;
    @Min(value = 1, message = "> 1")
    private int price;
}
