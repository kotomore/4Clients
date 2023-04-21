package ru.set404.telegramservice.dto.telegram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentMSG {
    private String id;
    private String name;
    private String phone;
    private String password;
}
