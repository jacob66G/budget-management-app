package com.example.budget_management_app.auth.mapper;

import com.example.budget_management_app.auth.dto.LoginResponse;
import com.example.budget_management_app.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public LoginResponse toLoginResponse(User user, String accessToken, boolean isMfaRequired) {
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getStatus(),
                user.isMfaEnabled(),
                user.getCreatedAt(),
                accessToken,
                isMfaRequired
        );
    }

}
