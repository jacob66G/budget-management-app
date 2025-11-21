package com.example.budget_management_app.user.mapper;

import com.example.budget_management_app.session.domain.UserSession;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.dto.UserResponseDto;
import com.example.budget_management_app.user.dto.UserSessionResponseDto;
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

    public UserSessionResponseDto toUserSessionResponseDto (UserSession session, Long currentSessionId) {
        boolean isCurrent = session.getId().equals(currentSessionId);

        return new UserSessionResponseDto(
                session.getId(),
                session.getIpAddress(),
                session.getDeviceInfo(),
                session.getDeviceType().name(),
                session.getCreatedAt(),
                session.getLastUsedAt(),
                isCurrent
        );
    }
}
