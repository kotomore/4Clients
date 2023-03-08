package ru.set404.clients.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class Therapist {
    private Long id;
    private String name;
    private String phone;
    private String password;
    private Role role;
    private List<Appointment> appointments;


    public Therapist() {
    }

    public Therapist(String name, String phone, String password, Role role) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Therapist therapist = (Therapist) o;
        return Objects.equals(id, therapist.id) && Objects.equals(name, therapist.name) && Objects.equals(phone, therapist.phone) && Objects.equals(password, therapist.password) && Objects.equals(role, therapist.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, phone, password, role);
    }

    @Override
    public String toString() {
        return "Therapist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}

