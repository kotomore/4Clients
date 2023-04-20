package ru.set404.telegramservice.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agent {
    private String id;
    private String name;
    private String phone;
    private String password;
}
