package ru.set404.clients.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "Services")
@NoArgsConstructor
public class Service {
    @Id
    private Long serviceId;
    private String name;
    private String description;
    private int duration;
    private int price;

    public Service(String name, String description, int duration, int price) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.price = price;
    }
}
