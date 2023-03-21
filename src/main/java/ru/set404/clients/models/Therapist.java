package ru.set404.clients.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Objects;

@Getter
@Setter
@Entity(name = "Therapists")
@NoArgsConstructor
public class Therapist {
    @Id
    @Column(name = "therapist_id")
    private Long id;
    private String name;
    private String phone;
    private String password;
    private Role role;

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

