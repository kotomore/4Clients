package ru.set404.clients.models;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    ADMIN("ADMIN"),
    USER("USER");

    private final String vale;

    Role(String vale) {
        this.vale = vale;
    }

    public String getVale() {
        return vale;
    }

    @Override
    public String getAuthority() {
        return vale;
    }
}
