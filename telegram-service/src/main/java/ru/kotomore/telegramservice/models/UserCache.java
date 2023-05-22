package ru.kotomore.telegramservice.models;

import ru.kotomore.telegramservice.enums.EntityEnum;

import java.util.List;

public class UserCache {
    private List<String> messageCache;
    private int currentPage;

    public UserCache(List<String> messageCache, int currentPage) {
        this.messageCache = messageCache;
        this.currentPage = currentPage;
    }

    public UserCache() {
    }

    public List<String> getMessageCache() {
        return messageCache;
    }

    public void setMessageCache(List<String> messageCache) {
        this.messageCache = messageCache;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
