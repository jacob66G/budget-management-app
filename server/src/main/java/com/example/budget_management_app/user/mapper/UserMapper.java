package com.example.budget_management_app.user.mapper;

import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.dto.UserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toUserResponseDto (User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getStatus().name(),
                user.isMfaEnabled(),
                user.getCreatedAt()
        );
    }
}
