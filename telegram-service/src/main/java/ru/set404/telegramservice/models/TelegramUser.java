package ru.set404.telegramservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name="telegram")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TelegramUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(unique = true)
    private String phone;
    @Column(unique = true)
    private String agentId;
    @Column(unique = true)
    private String chatId;
}
