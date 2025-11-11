package com.example.budget_management_app.auth.controller;

import com.example.budget_management_app.auth.dto.*;
import com.example.budget_management_app.auth.service.AuthService;
import com.example.budget_management_app.common.dto.ResponseMessageDto;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.UserSessionException;
import com.example.budget_management_app.constants.ApiConstants;
import com.example.budget_management_app.session.domain.UserSession;
import com.example.budget_management_app.session.dto.RefreshTokenResult;
import com.example.budget_management_app.session.service.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserSessionService userSessionService;

    @PostMapping("/register")
    public ResponseEntity<ResponseMessageDto> registerUser(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {
        ResponseMessageDto response = this.authService.registerUser(registrationRequestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> authenticate(
            @Valid @RequestBody LoginRequestDto loginRequestDto,
            HttpServletRequest request,
            @CookieValue(name = ApiConstants.REFRESH_TOKEN_COOKIE, required = false) String oldRefreshToken
    ) {
        LoginResponseDto response = authService.authenticateUser(loginRequestDto);
        if (response.isMfaEnabled()) {
            return ResponseEntity.ok(response);
        }

        return buildLoginResponseWithSession(response, request, oldRefreshToken);
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<LoginResponseDto> verifyTfa(
            @Valid @RequestBody TwoFactorLoginRequest loginRequestDto,
            HttpServletRequest request,
            @CookieValue(name = ApiConstants.REFRESH_TOKEN_COOKIE, required = false) String oldRefreshToken
    ) {
        LoginResponseDto response = authService.authenticateWith2fa(loginRequestDto);

        return buildLoginResponseWithSession(response, request, oldRefreshToken);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(
            HttpServletRequest request,
            @CookieValue(name = ApiConstants.REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            log.warn("Refresh token missing in request from IP {}", request.getRemoteAddr());
            throw new UserSessionException("No refresh token found in request", ErrorCode.TOKEN_NOT_FOUND);
        }
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        RefreshTokenResult result = userSessionService.refreshToken(refreshToken, userAgent);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.cookie())
                .body((result.loginResponse()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseMessageDto> logout(
            @CookieValue(name = ApiConstants.REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        if (StringUtils.hasText(refreshToken)) {
            userSessionService.logout(refreshToken);
        }

        ResponseCookie clearCookie = userSessionService.generateClearCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(new ResponseMessageDto("You have been logged out successfully."));
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyUser(@RequestParam String code) {
        authService.verifyUser(code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ResponseMessageDto> resendVerification(@Valid @RequestBody ResendVerificationRequestDto requestDto) {
        return ResponseEntity
                .ok(authService.resendVerification(requestDto.email()));
    }

    @PostMapping("/password-reset-confirm")
    public ResponseEntity<ResponseMessageDto> passwordResetConfirm(@Valid @RequestBody PasswordResetConfirmationDto resetConfirmDto) {
        authService.resetPasswordConfirm(resetConfirmDto);
        return ResponseEntity.ok(new ResponseMessageDto("Your password has been reset."));
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<ResponseMessageDto> passwordResetRequest(@Valid @RequestBody PasswordResetRequestDto requestDto) {
        return ResponseEntity
                .ok(authService.resetPassword(requestDto));
    }

    private ResponseEntity<LoginResponseDto> buildLoginResponseWithSession(LoginResponseDto response, HttpServletRequest request, String oldRefreshToken) {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        UserSession session = userSessionService.createUserSession(response.getUserId(), userAgent, oldRefreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, userSessionService.generateResponseCookie(session.getRawRefreshToken()).toString())
                .body(response);
    }
}
