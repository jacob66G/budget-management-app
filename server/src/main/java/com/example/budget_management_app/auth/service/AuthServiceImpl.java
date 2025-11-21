package com.example.budget_management_app.auth.service;

import com.example.budget_management_app.auth.dto.*;
import com.example.budget_management_app.auth.mapper.AuthMapper;
import com.example.budget_management_app.common.dto.ResponseMessageDto;
import com.example.budget_management_app.common.event.model.PasswordResetEvent;
import com.example.budget_management_app.common.event.model.VerificationEvent;
import com.example.budget_management_app.common.event.publisher.EventPublisher;
import com.example.budget_management_app.common.exception.*;
import com.example.budget_management_app.common.service.CacheService;
import com.example.budget_management_app.common.service.RedisServiceImpl;
import com.example.budget_management_app.security.service.JwtService;
import com.example.budget_management_app.security.service.TwoFactorAuthenticationService;
import com.example.budget_management_app.session.domain.UserSession;
import com.example.budget_management_app.session.service.UserSessionService;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.domain.UserStatus;
import com.example.budget_management_app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthMapper mapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EventPublisher eventPublisher;
    private final TwoFactorAuthenticationService tfaService;
    @Value("${security.verification-code.expiration}")
    private long verificationCodeExpiration;
    @Value("${security.verification-code.retry}")
    private long verificationRetryTime;
    @Value("${security.reset-password-token.expiration}")
    private long resetPasswordTokenExpiration;
    @Value("${security.reset-password-token.retry}")
    private long resetPasswordRetryTime;
    private final CacheService cacheService;
    private final UserService userService;
    private final UserSessionService sessionService;

    @Transactional
    @Override
    public ResponseMessageDto registerUser(RegistrationRequestDto dto) {
        User savedUser = userService.createUser(dto);

        sendVerification(savedUser, false);

        log.info("New user registration: email={}", savedUser.getEmail());
        return new ResponseMessageDto("Registration successful. Please check your email to activate your account.");
    }

    @Transactional
    @Override
    public LoginResult authenticateUser(LoginRequestDto loginRequest, HttpServletRequest request, String oldRefreshToken) {
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.email(), loginRequest.password());
        try {
            authenticationManager.authenticate(authentication);

        } catch (AuthenticationException e) {
            log.warn("Failed login attempt for email: {}", loginRequest.email());
            throw new BadCredentialsException("Invalid credentials");
        }

        User user = userService.findUserByEmail(loginRequest.email()).orElseThrow(() -> {
            log.warn("Login attempt for non-existent email: {}", loginRequest.email());
            return new NotFoundException(User.class.getSimpleName(), "email", loginRequest.email(), ErrorCode.USER_NOT_FOUND);
        });

        verifyUserStatus(user);

        if (user.isMfaEnabled()) {
            return LoginResult.mfaRequired(new LoginResponseDto(user.getId(), true));
        }

        LoginResult result = finalizeLogin(user, request, oldRefreshToken);

        log.info("User logged in: email={}", user.getEmail());
        return result;
    }

    @Transactional
    @Override
    public LoginResult authenticateWith2fa(TwoFactorLoginRequest loginRequest, HttpServletRequest request, String oldRefreshToken) {
        User user = userService.findUserById(loginRequest.userId())
                .orElseThrow(() -> new UsernameNotFoundException("User with id: " + loginRequest.userId() + " not found."));

        if (!user.isMfaEnabled()) {
            throw new TfaException("Multi-factor authentication (MFA) has not been configured.", ErrorCode.MFA_CONFIGURATION);
        }

        if (!tfaService.isOptValid(user.getSecret(), loginRequest.code())) {
            throw new BadCredentialsException("Invalid code");
        }

        LoginResult result = finalizeLogin(user, request, oldRefreshToken);
        log.info("User logged in: email={}", user.getEmail());
        return result;
    }

    @Transactional
    @Override
    public void verifyUser(String verificationCode) {
        String email = (String) cacheService.getValue(RedisServiceImpl.KeyPrefix.VERIFICATION_CODE, verificationCode);

        if (email == null) {
            log.warn("Verification attempt with invalid or expired code: {}", verificationCode);
            throw new VerificationCodeException("Invalid or expired verification code", ErrorCode.INVALID_CODE);
        }

        userService.activateUser(email);

        cacheService.delete(RedisServiceImpl.KeyPrefix.VERIFICATION_CODE, verificationCode);
        cacheService.delete(RedisServiceImpl.KeyPrefix.VERIFICATION_LAST_SENT, email);

        log.info("User email={} has verified account", email);
    }

    @Override
    public ResponseMessageDto resendVerification(String email) {
        ResponseMessageDto defaultResponse = new ResponseMessageDto("If an account with this email exists, a verification link has been sent.");
        Optional<User> optionalUser = userService.findUserByEmail(email);
        if (optionalUser.isEmpty()) {
            log.warn("User has tried resend verification with no exists email: {}", email);
            return defaultResponse;
        }

        User user = optionalUser.get();
        if (!user.getStatus().equals(UserStatus.PENDING_CONFIRMATION)) {
            log.warn("User email={} has tried resend verification with incorrect status", email);
            return defaultResponse;
        }

        if (isRateLimited(email, RedisServiceImpl.KeyPrefix.VERIFICATION_LAST_SENT, verificationRetryTime)) {
            return new ResponseMessageDto("You can request a new verification link later.");
        }

        sendVerification(user, true);

        log.info("User email={} has resent verification code", user.getEmail());
        return defaultResponse;
    }

    @Transactional
    @Override
    public void resetPasswordConfirm(PasswordResetConfirmationDto dto) {
        if (!dto.newPassword().equals(dto.confirmedNewPassword())) {
            throw new ValidationException("The provided passwords are different", ErrorCode.PASSWORDS_NOT_MATCH);
        }

        String email = (String) cacheService.getValue(RedisServiceImpl.KeyPrefix.RESET_PASSWORD_CODE, dto.token());

        if (email == null) {
            log.warn("Reset password attempt with invalid or expired token: {}", dto.token());
            throw new VerificationCodeException("Invalid or expired reset password token", ErrorCode.INVALID_CODE);
        }

        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new NotFoundException(User.class.getSimpleName(), "email", email, ErrorCode.USER_NOT_FOUND));

        userService.updateUserPassword(user.getId(), dto.newPassword());

        sessionService.deleteAllSessionsForUser(user.getId());

        cacheService.delete(RedisServiceImpl.KeyPrefix.RESET_PASSWORD_CODE, dto.token());
        cacheService.delete(RedisServiceImpl.KeyPrefix.RESET_PASSWORD_LAST_SENT, email);
    }

    @Transactional
    @Override
    public ResponseMessageDto resetPassword(PasswordResetRequestDto dto) {
        ResponseMessageDto defaultResponse = new ResponseMessageDto("If an account with this email exists, a reset password link has been sent.");
        Optional<User> optionalUser = userService.findUserByEmail(dto.email());

        if (optionalUser.isEmpty()) {
            log.warn("User has tried reset password with no exists email: {}", dto.email());
            return defaultResponse;
        }

        User user = optionalUser.get();

        if (!user.getStatus().equals(UserStatus.ACTIVE) && !user.getStatus().equals(UserStatus.PENDING_DELETION)) {
            log.warn("User email={} tried to reset password with incorrect status: {}", user.getEmail(), user.getStatus());
            return defaultResponse;
        }

        if (isRateLimited(user.getEmail(), RedisServiceImpl.KeyPrefix.RESET_PASSWORD_LAST_SENT, resetPasswordRetryTime)) {
            return new ResponseMessageDto("You can reset password later.");
        }

        sendResetPasswordToken(user);

        log.info("User email={} has sent reset password request", user.getEmail());
        return defaultResponse;
    }

    private LoginResult finalizeLogin(User user, HttpServletRequest request, String oldRefreshToken) {
        UserSession session = sessionService.createUserSession(user.getId(), request, oldRefreshToken);
        ResponseCookie cookie = sessionService.generateResponseCookie(session.getRawRefreshToken());

        String accessToken = jwtService.generateToken(user.getEmail(), session.getId());

        LoginResponseDto response = mapper.toLoginResponseDto(user, accessToken, false);
        return new LoginResult(response, cookie);
    }

    private String generateTokenValue() {
        return UUID.randomUUID().toString();
    }

    private void verifyUserStatus(User user) {
        UserStatus status = user.getStatus();
        if (status != UserStatus.ACTIVE) {
            if (status == UserStatus.PENDING_CONFIRMATION) {
                throw new UserNotAllowedToLoginException("User: " + user.getEmail() + " account is not confirmed", ErrorCode.USER_NOT_ACTIVE);
            }
            if (status == UserStatus.PENDING_DELETION) {
                userService.activateUser(user.getEmail());
            } else {
                throw new UserNotAllowedToLoginException("User: " + user.getEmail() + " account is not active", ErrorCode.USER_NOT_ACTIVE);
            }
        }
    }

    private boolean isRateLimited(String email, RedisServiceImpl.KeyPrefix keyPrefix, long retryTime) {
        String lastSentAtValue = (String) cacheService.getValue(keyPrefix, email);
        if (lastSentAtValue != null) {
            long lastSentAt = Long.parseLong(lastSentAtValue);
            if (Instant.now().isBefore(Instant.ofEpochMilli(lastSentAt).plusMillis(retryTime))) {
                log.warn("Rate limit hit for user email={}: {}", email, keyPrefix.name());
                return true;
            }
        }
        return false;
    }

    private void sendVerification(User user, boolean resend) {
        String verificationCode = generateTokenValue();
        cacheService.storeValue(RedisServiceImpl.KeyPrefix.VERIFICATION_CODE, verificationCode, user.getEmail(), verificationCodeExpiration);
        cacheService.storeValue(
                RedisServiceImpl.KeyPrefix.VERIFICATION_LAST_SENT,
                user.getEmail(),
                String.valueOf(Instant.now().toEpochMilli()),
                verificationRetryTime
        );
        eventPublisher.register(new VerificationEvent(user.getEmail(), user.getName(), verificationCode, resend));
    }

    private void sendResetPasswordToken(User user) {
        String token = generateTokenValue();
        cacheService.storeValue(RedisServiceImpl.KeyPrefix.RESET_PASSWORD_CODE, token, user.getEmail(), resetPasswordTokenExpiration);
        cacheService.storeValue(
                RedisServiceImpl.KeyPrefix.RESET_PASSWORD_LAST_SENT,
                user.getEmail(),
                String.valueOf(Instant.now().toEpochMilli()),
                resetPasswordRetryTime
        );

        eventPublisher.register(new PasswordResetEvent(user.getEmail(), user.getName(), token));
    }
}
