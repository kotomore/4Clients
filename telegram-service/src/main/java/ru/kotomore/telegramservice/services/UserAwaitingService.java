package ru.kotomore.telegramservice.services;

import lombok.Getter;
import org.springframework.stereotype.Service;
import ru.kotomore.telegramservice.constants.ActionDefinitionEnum;
import ru.kotomore.telegramservice.constants.ActionPartEnum;
import ru.kotomore.telegramservice.models.UserAwaitingResponse;

import java.util.ArrayList;
import java.util.List;

@Service
@Getter
public class UserAwaitingService {
    private static final List<UserAwaitingResponse> users = new ArrayList<>();

    public void addToWaitingList(
            String chatId,
            ActionPartEnum actionPartEnum,
            ActionDefinitionEnum actionDefinitionEnum) {
        UserAwaitingResponse userAwaitingResponse = new UserAwaitingResponse(chatId, actionPartEnum, actionDefinitionEnum);
        users.add(userAwaitingResponse);
    }

    public void removeFromWaitingList(String chatId) {
        users.removeIf(userAwaitingResponse -> userAwaitingResponse.chatId().equals(chatId));
    }

    public boolean contains(String chatId) {
        return getWaiter(chatId) != null;
    }

    public UserAwaitingResponse getWaiter(String chatId) {
        for (UserAwaitingResponse userAwaitingResponse : users) {
            if (userAwaitingResponse.chatId().equals(chatId)) {
                return userAwaitingResponse;
            }
        }
        return null;
    }
}