package com.example.budget_management_app.auth.mapper;

import com.example.budget_management_app.auth.dto.LoginResponseDto;
import com.example.budget_management_app.auth.dto.RegistrationResponseDto;
import com.example.budget_management_app.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public RegistrationResponseDto toRegistrationResponseDto(User user) {
        return new RegistrationResponseDto(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getStatus().name()
        );
    }

    public LoginResponseDto toLoginResponseDto(User user, String accessToken) {
        return new LoginResponseDto(
                accessToken,
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.isMfaEnabled()
        );
    }

}
