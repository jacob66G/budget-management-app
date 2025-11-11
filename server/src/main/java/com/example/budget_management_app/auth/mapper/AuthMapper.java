package com.example.budget_management_app.auth.mapper;

import com.example.budget_management_app.auth.dto.LoginResponseDto;
import com.example.budget_management_app.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public LoginResponseDto toLoginResponseDto(User user, String accessToken, boolean isMfaRequired) {
        return new LoginResponseDto(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getStatus().name(),
                user.isMfaEnabled(),
                user.getCreatedAt(),
                accessToken,
                isMfaRequired
        );
    }

}
