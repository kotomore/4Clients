package ru.kotomore.taptimes.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "clients")
public class Client {
    private String name;
    private String phone;
}
