package ru.set404.clients.models;

public class Service {
    private Long serviceId;
    private String name;
    private String description;
    private int duration;
    private int price;

    public Service(Long serviceId, String name, String description, int duration, int price) {
        this.serviceId = serviceId;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.price = price;
    }

    public Service() {
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
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
