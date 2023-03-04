package ru.set404.clients.models;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Objects;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
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

