package com.example.budget_management_app.auth.service;

import com.example.budget_management_app.auth.dto.*;
import com.example.budget_management_app.auth.mapper.AuthMapper;
import com.example.budget_management_app.common.dto.ResponseMessageDto;
import com.example.budget_management_app.common.event.model.VerificationEvent;
import com.example.budget_management_app.common.event.publisher.EventPublisher;
import com.example.budget_management_app.common.exception.*;
import com.example.budget_management_app.security.service.JwtService;
import com.example.budget_management_app.security.service.TwoFactorAuthenticationService;
import com.example.budget_management_app.user.dao.UserDao;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserDao userDao;
    private final PasswordEncoder encoder;
    private final AuthMapper mapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EventPublisher eventPublisher;
    private final TwoFactorAuthenticationService tfaService;
    @Value("${security.verification-code.expiration}")
    private long verificationCodeExpiration;
    @Value("${security.verification-code.retry}")
    private long verificationRetryTime;

    @Transactional
    public RegistrationResponseDto registerUser(RegistrationRequestDto requestDto) {
        checkEmailUniqueness(requestDto.email());

        User user = new User();
        user.setName(requestDto.name());
        user.setSurname(requestDto.surname());
        user.setEmail(requestDto.email());
        user.setEmailLastChanged(Instant.now());
        user.setPassword(encoder.encode(requestDto.password()));
        user.setStatus(UserStatus.PENDING_CONFIRMATION);
        user.setCreatedAt(Instant.now());

        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(Instant.now().plusMillis(verificationCodeExpiration));
        user.setLastVerificationSentAt(Instant.now());
        //user.setCategories();\

        User savedUser = userDao.save(user);
        eventPublisher.register(new VerificationEvent(savedUser.getEmail(), savedUser.getName(), verificationCode, false));

        log.info("New user registration: email={}", user.getEmail());
        return mapper.toRegistrationResponseDto(savedUser);
    }

    public LoginResponseDto authenticateUser(LoginRequestDto loginRequest) {
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.email(), loginRequest.password());
        try {
           authenticationManager.authenticate(authentication);

        } catch (AuthenticationException e) {
            log.warn("Failed login attempt for email: {}", loginRequest.email());
            throw new BadCredentialsException("Invalid credentials");
        }

        User user = userDao.findByEmail(loginRequest.email()).orElseThrow(() -> {
            log.warn("Login attempt for non-existent email: {}", loginRequest.email());
            return new NotFoundException(User.class.getSimpleName(), "email", loginRequest.email(), ErrorCode.USER_NOT_FOUND);
        });

        verifyUserStatus(user);

        if (user.isMfaEnabled()) {
            return new LoginResponseDto(user.getId(), true);
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        log.info("User logged in: email={}", user.getEmail());

        return mapper.toLoginResponseDto(user, accessToken);
    }

    public LoginResponseDto authenticateWith2fa(TwoFactorLoginRequest loginRequest) {
        User user = userDao.findById(loginRequest.userId())
                .orElseThrow(() -> new UsernameNotFoundException("User with id: " + loginRequest.userId() + " not found."));

        if (!user.isMfaEnabled()) {
            throw new TfaException("Multi-factor authentication (MFA) has not been configured.", ErrorCode.MFA_CONFIGURATION);
        }

        if (!tfaService.isOptValid(user.getSecret(), loginRequest.code())) {
            throw new BadCredentialsException("Invalid code");
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        log.info("User logged in: email={}", user.getEmail());

        return mapper.toLoginResponseDto(user, accessToken);
    }

    @Transactional
    public void verifyUser(String verificationCode) {
        User user = userDao.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new VerificationCodeException("Invalid verification code", ErrorCode.INVALID_CODE));
        if (user.getVerificationCodeExpiresAt().isBefore(Instant.now())) {
            log.warn("Verification attempt with expired code: {}", user.getEmail());
            throw new VerificationCodeException("Verification code has expired", ErrorCode.CODE_EXPIRED);
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        log.info("User email={} has verified account", user.getEmail());
    }

    @Transactional
    public ResponseMessageDto resendVerification(String email) {
        Optional<User> optionalUser = userDao.findByEmail(email);

        if (optionalUser.isEmpty()) {
            log.warn("User has tried resend verification with no exists email: {}", email);
            return new ResponseMessageDto("If an account with this email exists, a verification link has been sent.");
        }

        User user = optionalUser.get();
        if (user.getStatus() == UserStatus.PENDING_CONFIRMATION && StringUtils.hasText(user.getVerificationCode())) {
            if(!user.getLastVerificationSentAt().plusMillis(verificationRetryTime).isBefore(Instant.now())) {
                log.warn("User email={} has tried resend verification again", user.getEmail());
                return new ResponseMessageDto("You can request a new verification link later.");
            }
            String verificationCode = generateVerificationCode();
            user.setVerificationCode(verificationCode);
            user.setVerificationCodeExpiresAt(Instant.now().plusMillis(verificationCodeExpiration));
            user.setLastVerificationSentAt(Instant.now());
            userDao.save(user);

            eventPublisher.register(new VerificationEvent(email, user.getName(), verificationCode, true));
        }

        log.info("User email={} has resent verification code", user.getEmail());
        return new ResponseMessageDto("If an account with this email exists, a verification link has been sent.");
    }

    private void checkEmailUniqueness(String email) {
        if (userDao.findByEmail(email).isPresent()) {
            throw new ValidationException("This email: " + email + " is already used", ErrorCode.EMAIL_ALREADY_USED);
        }
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString();
    }

    private void verifyUserStatus(User user) {
        UserStatus status = user.getStatus();
        if (status != UserStatus.ACTIVE) {
            if (status == UserStatus.PENDING_CONFIRMATION) {
                throw new UserNotAllowedToLoginException("User: " + user.getEmail() + " account is not confirmed", ErrorCode.USER_NOT_ACTIVE);
            }
            if (status == UserStatus.PENDING_DELETION) {
                ///TODO handle user activation
            } else {
                throw new UserNotAllowedToLoginException("User: " + user.getEmail() + " account is not active", ErrorCode.USER_NOT_ACTIVE);
            }
        }
    }
}
