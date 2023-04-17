package ru.set404.clients.models;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "clients")
public class Client {
    @Id
    private String id;
    private String name;
    @Indexed(unique = true)
    private String phone;
}
