package ru.set404.clients.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Service {
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

    public Service() {
    }
}
