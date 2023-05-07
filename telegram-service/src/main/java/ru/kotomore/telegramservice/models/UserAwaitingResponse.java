package ru.kotomore.telegramservice.models;

import ru.kotomore.telegramservice.constants.ActionDefinitionEnum;
import ru.kotomore.telegramservice.constants.ActionPartEnum;

import java.util.Objects;

public record UserAwaitingResponse(String chatId, ActionPartEnum actionPart, ActionDefinitionEnum definition) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAwaitingResponse that = (UserAwaitingResponse) o;
        return Objects.equals(chatId, that.chatId) && actionPart == that.actionPart && definition == that.definition;
    }
}
