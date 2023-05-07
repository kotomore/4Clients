package ru.kotomore.taptimes.models;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    ADMIN("ADMIN"),
    USER("USER");

    private final String vale;

    Role(String vale) {
        this.vale = vale;
    }

    public String getValue() {
        return vale;
    }

    @Override
    public String getAuthority() {
        return vale;
    }
}
