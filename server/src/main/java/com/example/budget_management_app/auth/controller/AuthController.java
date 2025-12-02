package com.example.budget_management_app.auth.controller;

import com.example.budget_management_app.auth.dto.*;
import com.example.budget_management_app.auth.service.AuthService;
import com.example.budget_management_app.common.dto.ResponseMessage;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.UserSessionException;
import com.example.budget_management_app.constants.ApiConstants;
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
    public ResponseEntity<ResponseMessage> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        ResponseMessage response = this.authService.registerUser(registrationRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            @CookieValue(name = ApiConstants.REFRESH_TOKEN_COOKIE, required = false) String oldRefreshToken
    ) {
        LoginResult result = authService.authenticateUser(loginRequest, request, oldRefreshToken);

        if (result.response().getIsMfaRequired()) {
            return ResponseEntity.ok(result.response());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.cookie().toString())
                .body(result.response());
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<LoginResponse> verifyTfa(
            @Valid @RequestBody TwoFactorLoginRequest loginRequestDto,
            HttpServletRequest request,
            @CookieValue(name = ApiConstants.REFRESH_TOKEN_COOKIE, required = false) String oldRefreshToken
    ) {
        LoginResult result = authService.authenticateWith2fa(loginRequestDto, request, oldRefreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.cookie().toString())
                .body(result.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            HttpServletRequest request,
            @CookieValue(name = ApiConstants.REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            log.warn("Refresh token missing in request from IP {}", request.getRemoteAddr());
            throw new UserSessionException("No refresh token found in request", ErrorCode.TOKEN_NOT_FOUND);
        }
        RefreshTokenResult result = userSessionService.refreshToken(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.cookie())
                .body((result.loginResponse()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = ApiConstants.REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        if (StringUtils.hasText(refreshToken)) {
            userSessionService.logout(refreshToken);
        }

        ResponseCookie clearCookie = userSessionService.generateClearCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString()).build();
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyUser(@RequestParam String code) {
        authService.verifyUser(code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ResponseMessage> resendVerification(@Valid @RequestBody ResendVerificationRequest requestDto) {
        return ResponseEntity
                .ok(authService.resendVerification(requestDto.email()));
    }

    @PostMapping("/password-reset-confirm")
    public ResponseEntity<ResponseMessage> passwordResetConfirm(@Valid @RequestBody PasswordResetConfirmation resetConfirmDto) {
        authService.resetPasswordConfirm(resetConfirmDto);
        return ResponseEntity.ok(new ResponseMessage("Your password has been reset."));
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<ResponseMessage> passwordResetRequest(@Valid @RequestBody PasswordResetRequest requestDto) {
        return ResponseEntity
                .ok(authService.resetPassword(requestDto));
    }
}
