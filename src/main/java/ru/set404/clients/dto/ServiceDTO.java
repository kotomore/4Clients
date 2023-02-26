package ru.set404.clients.dto;

public class ServiceDTO {
    private String name;
    private String description;
    private int duration;
    private int price;

    public ServiceDTO(String name, String description, int duration, int price) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.price = price;
    }

    public ServiceDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
