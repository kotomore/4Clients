package ru.set404.clients.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.set404.clients.models.Therapist;

import java.util.Collection;
import java.util.Collections;

@Getter
public class TherapistDetails implements UserDetails {

    private final Therapist therapist;

    public TherapistDetails(Therapist therapist) {
        this.therapist = therapist;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(therapist.getRole());
    }

    @Override
    public String getPassword() {
        return therapist.getPassword();
    }

    @Override
    public String getUsername() {
        return therapist.getPhone();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
