package ru.set404.telegramservice.telegram.util;

import ru.set404.telegramservice.enums.CallbackActionDefinitionEnum;
import ru.set404.telegramservice.enums.CallbackActionPartsEnum;

import java.util.Objects;

public record NeedAnswer(String chatId, CallbackActionPartsEnum actionPart, CallbackActionDefinitionEnum definition) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeedAnswer that = (NeedAnswer) o;
        return Objects.equals(chatId, that.chatId) && actionPart == that.actionPart && definition == that.definition;
    }
}
