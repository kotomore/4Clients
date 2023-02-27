package ru.set404.clients.dto;


import ru.set404.clients.models.Client;

import java.util.Objects;

public class ClientDTO {
    private String name;
    private String phone;

    public ClientDTO() {
    }

    public String getName() {
        return name;
    }

    public Client toClient() {
        return new Client(name, phone);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
