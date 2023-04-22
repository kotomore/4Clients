package ru.set404.telegramservice.models;

import ru.set404.telegramservice.constants.ActionDefinitionEnum;
import ru.set404.telegramservice.constants.ActionPartEnum;

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
