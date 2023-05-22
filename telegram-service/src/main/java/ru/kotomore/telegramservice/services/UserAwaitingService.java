package ru.kotomore.telegramservice.services;

import lombok.Getter;
import org.springframework.stereotype.Service;
import ru.kotomore.telegramservice.enums.DefinitionEnum;
import ru.kotomore.telegramservice.enums.EntityEnum;
import ru.kotomore.telegramservice.models.UserAwaitingResponse;
import ru.kotomore.telegramservice.models.UserCache;

import java.util.*;

@Service
@Getter
public class UserAwaitingService {
    private static final List<UserAwaitingResponse> users = new ArrayList<>();
    private static final Map<String, UserCache> userCacheMap = new HashMap<>();


    public void addMessageToCache(String chatId, EntityEnum entityEnum, List<String> messages) {
        String cacheKey = chatId + "_" + entityEnum;
        userCacheMap.put(cacheKey, new UserCache(messages, 0));
    }

    public String getNextMessageFromCache(String chatId, EntityEnum entityEnum) {
        String cacheKey = chatId + "_" + entityEnum;
        UserCache cache = userCacheMap.get(cacheKey);

        if (cache != null) {
            List<String> messageCache = cache.getMessageCache();
            int currentPage = cache.getCurrentPage();
            int nextPage = (currentPage + 1) % messageCache.size();
            cache.setCurrentPage(nextPage);
            return messageCache.get(nextPage);
        }
        return "";
    }

    public String getPreviousMessageFromCache(String chatId, EntityEnum entityEnum) {
        String cacheKey = chatId + "_" + entityEnum;
        UserCache cache = userCacheMap.get(cacheKey);

        if (cache != null) {
            List<String> messageCache = cache.getMessageCache();
            int currentPage = cache.getCurrentPage();
            int previousPage = (currentPage - 1 + messageCache.size()) % messageCache.size();
            cache.setCurrentPage(previousPage);
            return messageCache.get(previousPage);
        }
        return "";
    }

    public void clearUserCache(String chatId, EntityEnum entityEnum) {
        String cacheKey = chatId + "_" + entityEnum;
        userCacheMap.remove(cacheKey);
    }

    public void addToWaitingList(
            String chatId,
            EntityEnum entityEnum,
            DefinitionEnum definitionEnum) {
        UserAwaitingResponse userAwaitingResponse = new UserAwaitingResponse(chatId, entityEnum, definitionEnum);
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