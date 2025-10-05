package com.example.budget_management_app.session.service;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.InternalException;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.UserSessionException;
import com.example.budget_management_app.common.service.CacheService;
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
    private final CacheService cacheService;

    public RefreshTokenResult refreshToken(String token, String userAgent) {
        UserSession userSession = validateRefreshToken(token);

        User user = userSession.getUser();
        String accessToken = jwtService.generateToken(user.getEmail());
        String newRefreshTokenValue = rotateRefreshToken(userSession, userAgent, token);

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
        userSession.setCreatedAt(Instant.now());

        String refreshToken = generateTokenValue();
        userSession.setRawRefreshToken(refreshToken);
        user.addSession(userSession);

        UserSession persistedSession = userSessionDao.save(userSession);

        String refreshTokenHash = TokenHasher.hash(refreshToken);
        cacheService.storeValue(CacheService.KeyPrefix.REFRESH_TOKEN, refreshTokenHash, String.valueOf(persistedSession.getId()), refreshTokenExpiration);

        return persistedSession;
    }

    public ResponseCookie generateResponseCookie(String refreshToken) {
        return ResponseCookie.from(ApiConstants.REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .maxAge(refreshTokenExpiration)
                .sameSite("None")
                .secure(false)
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
        String userSessionId = cacheService.getValue(CacheService.KeyPrefix.REFRESH_TOKEN, tokenHash);
        if (userSessionId == null)  {
            throw new UserSessionException("Invalid token or session has expired", ErrorCode.INVALID_TOKEN);
        }

        return userSessionDao.findById(Long.valueOf(userSessionId))
                .orElseThrow(()-> new UserSessionException("Invalid token", ErrorCode.INVALID_TOKEN));
    }

    private String rotateRefreshToken(UserSession session, String userAgent, String oldToken) {
        String newToken = generateTokenValue();
        session.setUserAgent(userAgent);
        userSessionDao.save(session);

        cacheService.delete(CacheService.KeyPrefix.REFRESH_TOKEN, TokenHasher.hash(oldToken));
        cacheService.storeValue(CacheService.KeyPrefix.REFRESH_TOKEN, TokenHasher.hash(newToken), String.valueOf(session.getId()), refreshTokenExpiration);

        return newToken;
    }

    private String generateTokenValue() {
        return UUID.randomUUID().toString();
    }

}
