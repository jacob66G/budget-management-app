package com.example.budget_management_app.security.service;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String email;
    private final String password;
    private final Long sessionId;

    public CustomUserDetails(Long id, String email, String password, Long sessionId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.sessionId = sessionId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
