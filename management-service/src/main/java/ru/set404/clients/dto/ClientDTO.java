package ru.set404.clients.dto;


import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.set404.clients.models.Client;

@NoArgsConstructor
@Getter
@Setter
public class ClientDTO {
    @NotEmpty(message = "Not be empty")
    private String name;
    @NotEmpty(message = "Not be empty")
    private String phone;

    public Client toClient() {
        return new Client(name, phone);
    }
}
