package com.example.budget_management_app.session.service;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.InternalException;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.UserSessionException;
import com.example.budget_management_app.constants.ApiConstants;
import com.example.budget_management_app.security.service.JwtService;
import com.example.budget_management_app.session.dao.UserSessionDao;
import com.example.budget_management_app.session.domain.UserSession;
import com.example.budget_management_app.session.dto.RefreshTokenResult;
import com.example.budget_management_app.user.dao.UserDao;
import com.example.budget_management_app.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserSessionService {

    @Value("${security.refresh-token.expiration}")
    private long refreshTokenExpiration;
    @Value("${config.session.max-sessions}")
    private long maxSessions;
    private final UserSessionDao userSessionDao;
    private final UserDao userDao;
    private final JwtService jwtService;

    public RefreshTokenResult refreshToken(String token, String userAgent) {
        UserSession userSession = validateRefreshToken(token);

        User user = userSession.getUser();
        String accessToken = jwtService.generateToken(user.getEmail());
        String newRefreshTokenValue = rotateRefreshToken(userSession, userAgent);

        ResponseCookie cookie = generateResponseCookie(newRefreshTokenValue);

        log.info("User email={} has refreshed token", user.getEmail());
        return new RefreshTokenResult(accessToken, cookie.toString());
    }

    public UserSession createUserSession(Long userId, String userAgent) {
        User user = userDao.findById(userId)
                .orElseThrow(()-> new NotFoundException(User.class.getSimpleName(), userId, ErrorCode.USER_NOT_FOUND));

        enforceMaxSessions(user);

        UserSession userSession = new UserSession();
        userSession.setUserAgent(userAgent);
        userSession.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiration));
        userSession.setCreatedAt(Instant.now());

        String refreshToken = generateTokenValue();
        userSession.setRawRefreshToken(refreshToken);
        userSession.setRefreshToken(TokenHasher.hash(refreshToken));
        user.addSession(userSession);

        return userSessionDao.save(userSession);
    }

    public ResponseCookie generateResponseCookie(String refreshToken) {
        return ResponseCookie.from(ApiConstants.REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .maxAge(refreshTokenExpiration)
                .sameSite("None")
                .path("/api/auth/refresh")
                .build();
    }

    private void enforceMaxSessions(User user) {
        List<UserSession> sessions = user.getSessions();
        if (sessions.size() >= maxSessions) {
            UserSession oldestSession =  sessions.stream()
                    .min(Comparator.comparing(UserSession::getCreatedAt))
                    .orElseThrow(() -> new InternalException("Oldest session not found"));

            user.removeSession(oldestSession);
        }
    }

    public UserSession validateRefreshToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new UserSessionException("Token cannot be empty", ErrorCode.INVALID_TOKEN);
        }
        String tokenHash = TokenHasher.hash(token);
        UserSession userSession = userSessionDao.findByToken(tokenHash)
                .orElseThrow(()-> new UserSessionException("Invalid token", ErrorCode.SESSION_EXPIRED));

        if(isExpired(userSession)) {
            throw new UserSessionException("Session has expired", ErrorCode.SESSION_EXPIRED);
        }
        return userSession;
    }

    private String rotateRefreshToken(UserSession session, String userAgent) {
        String newToken = generateTokenValue();
        session.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiration));
        session.setRefreshToken(TokenHasher.hash(newToken));
        session.setUserAgent(userAgent);
        userSessionDao.save(session);

        return newToken;
    }

    private boolean isExpired(UserSession token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }

    private String generateTokenValue() {
        return UUID.randomUUID().toString();
    }
}
