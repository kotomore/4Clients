package ru.kotomore.telegramservice.models;

import ru.kotomore.telegramservice.enums.DefinitionEnum;
import ru.kotomore.telegramservice.enums.EntityEnum;

import java.util.Objects;

public record UserAwaitingResponse(String chatId, EntityEnum entity, DefinitionEnum definition) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAwaitingResponse that = (UserAwaitingResponse) o;
        return Objects.equals(chatId, that.chatId) && entity == that.entity && definition == that.definition;
    }
}
