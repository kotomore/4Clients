package ru.set404.telegramservice.services;

import lombok.Getter;
import org.springframework.stereotype.Service;
import ru.set404.telegramservice.enums.CallbackActionDefinitionEnum;
import ru.set404.telegramservice.enums.CallbackActionPartsEnum;
import ru.set404.telegramservice.telegram.util.NeedAnswer;

import java.util.ArrayList;
import java.util.List;

@Service
@Getter
public class WaitAnswerService {
    private final List<NeedAnswer> waiterServices = new ArrayList<>();

    public void addToWaitingList(
            String chatId,
            CallbackActionPartsEnum callbackActionPartsEnum,
            CallbackActionDefinitionEnum callbackActionDefinitionEnum) {
        NeedAnswer needAnswer = new NeedAnswer(chatId, callbackActionPartsEnum, callbackActionDefinitionEnum);
        waiterServices.add(needAnswer);
    }

    public void removeFromWaitingList(String chatId) {
        waiterServices.removeIf(needAnswer -> needAnswer.chatId().equals(chatId));
    }

    public boolean contains(String chatId) {
        return getWaiter(chatId) != null;
    }

    public NeedAnswer getWaiter(String chatId) {
        for (NeedAnswer needAnswer : waiterServices) {
            if (needAnswer.chatId().equals(chatId)) {
                return needAnswer;
            }
        }
        return null;
    }
}