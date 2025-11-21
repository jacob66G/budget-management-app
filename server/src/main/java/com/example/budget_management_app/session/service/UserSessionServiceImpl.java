package com.example.budget_management_app.session.service;

import com.example.budget_management_app.auth.dto.LoginResponseDto;
import com.example.budget_management_app.auth.mapper.AuthMapper;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.InternalException;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.UserSessionException;
import com.example.budget_management_app.common.service.CacheService;
import com.example.budget_management_app.common.service.RedisServiceImpl;
import com.example.budget_management_app.constants.ApiConstants;
import com.example.budget_management_app.security.service.JwtService;
import com.example.budget_management_app.session.dao.UserSessionDao;
import com.example.budget_management_app.session.domain.UserSession;
import com.example.budget_management_app.session.dto.RefreshTokenResult;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {

    @Value("${security.refresh-token.expiration}")
    private long refreshTokenExpiration;
    @Value("${config.session.max-sessions}")
    private long maxSessions;
    private final UserSessionDao userSessionDao;
    private final UserService userService;
    private final JwtService jwtService;
    private final CacheService cacheService;
    private final AuthMapper authMapper;
    private final ClientInfoService clientInfoService;

    @Transactional
    @Override
    public RefreshTokenResult refreshToken(String token) {
        UserSession userSession = validateRefreshToken(token);

        User user = userSession.getUser();
        String accessToken = jwtService.generateToken(user.getEmail(), userSession.getId());
        String newRefreshTokenValue = rotateRefreshToken(userSession, token);

        ResponseCookie cookie = generateResponseCookie(newRefreshTokenValue);
        LoginResponseDto loginResponse = authMapper.toLoginResponseDto(user, accessToken, false);

        log.info("User email={} has refreshed token", user.getEmail());
        return new RefreshTokenResult(loginResponse, cookie.toString());
    }

    @Transactional
    @Override
    public UserSession createUserSession(Long userId, HttpServletRequest request, String oldRefreshToken) {
        User user = userService.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(User.class.getSimpleName(), userId, ErrorCode.USER_NOT_FOUND));

        ClientInfoService.ClientMetadata metadata = clientInfoService.extract(request);

        if (StringUtils.hasText(oldRefreshToken)) {
            Optional<UserSession> oldSessionOpt = findSessionByToken(oldRefreshToken);

            if (oldSessionOpt.isPresent()) {
                UserSession oldSession = oldSessionOpt.get();
                if (oldSession.getUser().getId().equals(userId)) {
                    log.info("Login: Invalidating old session {} for user {}", oldSession.getId(), userId);
                    user.removeSession(oldSession);
                    userSessionDao.delete(oldSession);
                    evictUserSessionFromCache(oldSession);
                }
            }
        }

        enforceMaxSessions(user);

        UserSession userSession = new UserSession();
        userSession.setIpAddress(metadata.ip());
        userSession.setDeviceInfo(metadata.deviceInfo());
        userSession.setDeviceType(metadata.type());

        String refreshToken = generateTokenValue();
        userSession.setRawRefreshToken(refreshToken);
        user.addSession(userSession);

        UserSession persistedSession = userSessionDao.save(userSession);

        String refreshTokenHash = TokenHasher.hash(refreshToken);
        cacheUserSession(refreshTokenHash, persistedSession);

        return persistedSession;
    }

    @Transactional
    @Override
    public void deleteAllSessionsForUser(Long userId) {
        List<UserSession> sessions = userSessionDao.findAllByUserId(userId);

        if (sessions.isEmpty()) {
            return;
        }

        List<String> sessionIdsToClear = new ArrayList<>();
        List<String> tokenHashesToClear = new ArrayList<>();

        for (UserSession session : sessions) {
            sessionIdsToClear.add(String.valueOf(session.getId()));

            String tokenHash = (String) cacheService.getValue(RedisServiceImpl.KeyPrefix.USER_SESSION, String.valueOf(session.getId()));
            if (tokenHash != null) {
                tokenHashesToClear.add(tokenHash);
            }
        }

        cacheService.delete(RedisServiceImpl.KeyPrefix.USER_SESSION, sessionIdsToClear);
        cacheService.delete(RedisServiceImpl.KeyPrefix.REFRESH_TOKEN, tokenHashesToClear);

        userSessionDao.deleteAllByUserId(userId);
    }

    @Transactional
    @Override
    public void logout(Long sessionId, Long userId) {
        UserSession userSession = userSessionDao.findByIdAndUser(sessionId, userId)
                .orElseThrow(() -> new NotFoundException(UserSession.class.getSimpleName(), sessionId, ErrorCode.NOT_FOUND));

        performLogoutCleanup(userSession);
    }

    @Transactional
    @Override
    public void logout(String refreshToken) {
        Optional<UserSession> sessionOpt = findSessionByToken(refreshToken);

        if (sessionOpt.isEmpty()) {
            log.warn("Attempt to log out with a non-existent or already invalidated token.");
            return;
        }

        UserSession session = sessionOpt.get();

        performLogoutCleanup(session);
    }

    @Override
    public ResponseCookie generateResponseCookie(String refreshToken) {
        return ResponseCookie.from(ApiConstants.REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .maxAge(refreshTokenExpiration)
                .sameSite("Lax")
                .secure(false)
                .path("/api/auth")
                .build();
    }

    @Override
    public ResponseCookie generateClearCookie() {
        return ResponseCookie.from(ApiConstants.REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .maxAge(0)
                .sameSite("Lax")
                .secure(false)
                .path("/api/auth")
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserSession> findSessionsByUser(Long userId) {
        return userSessionDao.findAllByUserId(userId);
    }

    private void performLogoutCleanup(UserSession session) {
        evictUserSessionFromCache(session);
        userSessionDao.delete(session);
        log.info("User {} has successfully logged out (session {}).", session.getUser().getId(), session.getId());
    }

    private void enforceMaxSessions(User user) {
        List<UserSession> sessions = user.getSessions();
        if (sessions.size() >= maxSessions) {
            UserSession oldestSession = sessions.stream()
                    .min(Comparator.comparing(UserSession::getCreatedAt))
                    .orElseThrow(() -> new InternalException("Oldest session not found"));

            user.removeSession(oldestSession);
            evictUserSessionFromCache(oldestSession);
        }
    }

    private UserSession validateRefreshToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new UserSessionException("Token cannot be empty", ErrorCode.INVALID_TOKEN);
        }

        String tokenHash = TokenHasher.hash(token);
        String userSessionId = (String) cacheService.getValue(RedisServiceImpl.KeyPrefix.REFRESH_TOKEN, tokenHash);
        if (userSessionId == null) {
            throw new UserSessionException("Invalid token or session has expired", ErrorCode.INVALID_TOKEN);
        }

        return userSessionDao.findById(Long.valueOf(userSessionId))
                .orElseThrow(() -> new UserSessionException("Invalid token", ErrorCode.INVALID_TOKEN));
    }

    private String rotateRefreshToken(UserSession session, String oldToken) {
        String newToken = generateTokenValue();
        session.setLastUsedAt(Instant.now());
        userSessionDao.save(session);

        cacheService.delete(RedisServiceImpl.KeyPrefix.REFRESH_TOKEN, TokenHasher.hash(oldToken));
        cacheService.delete(RedisServiceImpl.KeyPrefix.USER_SESSION, String.valueOf(session.getId()));

        String newTokenHash = TokenHasher.hash(newToken);
        cacheUserSession(newTokenHash, session);

        return newToken;
    }

    private String generateTokenValue() {
        return UUID.randomUUID().toString();
    }

    private void cacheUserSession(String refreshTokenHash, UserSession persistedSession) {
        cacheService.storeValue(RedisServiceImpl.KeyPrefix.REFRESH_TOKEN, refreshTokenHash, String.valueOf(persistedSession.getId()), refreshTokenExpiration);
        cacheService.storeValue(RedisServiceImpl.KeyPrefix.USER_SESSION, String.valueOf(persistedSession.getId()), refreshTokenHash, refreshTokenExpiration);
    }

    private void evictUserSessionFromCache(UserSession userSession) {
        String refreshToken = (String) cacheService.getValue(RedisServiceImpl.KeyPrefix.USER_SESSION, String.valueOf(userSession.getId()));
        if (refreshToken != null) {
            cacheService.delete(RedisServiceImpl.KeyPrefix.REFRESH_TOKEN, refreshToken);
        }
        cacheService.delete(RedisServiceImpl.KeyPrefix.USER_SESSION, String.valueOf(userSession.getId()));
    }

    private Optional<UserSession> findSessionByToken(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        String tokenHash = TokenHasher.hash(token);
        String userSessionId = (String) cacheService.getValue(RedisServiceImpl.KeyPrefix.REFRESH_TOKEN, tokenHash);
        if (userSessionId == null) {
            return Optional.empty();
        }
        return userSessionDao.findById(Long.valueOf(userSessionId));
    }

}
