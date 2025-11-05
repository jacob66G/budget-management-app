package com.example.budget_management_app.session.service;

import com.example.budget_management_app.session.domain.UserSession;
import com.example.budget_management_app.session.dto.RefreshTokenResult;
import org.springframework.http.ResponseCookie;

import java.util.List;

public interface UserSessionService {

    RefreshTokenResult refreshToken(String token, String userAgent);

    UserSession createUserSession(Long userId, String userAgent, String oldRefreshToken);

    ResponseCookie generateResponseCookie(String refreshToken);

    void deleteAllSessionsForUser(Long id);

    void logout(Long sessionId, Long userId);

    void logout(String refreshToken);

    ResponseCookie generateClearCookie();

    List<UserSession> findSessionsByUser(Long userId);
}
