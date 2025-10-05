package com.example.budget_management_app.auth.controller;

import com.example.budget_management_app.auth.dto.*;
import com.example.budget_management_app.auth.service.AuthService;
import com.example.budget_management_app.common.dto.ResponseMessageDto;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.UserSessionException;
import com.example.budget_management_app.constants.ApiConstants;
import com.example.budget_management_app.constants.ApiPaths;
import com.example.budget_management_app.session.domain.UserSession;
import com.example.budget_management_app.session.dto.RefreshTokenResult;
import com.example.budget_management_app.session.service.UserSessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.WebUtils;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserSessionService userSessionService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDto> registerUser(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {
        RegistrationResponseDto response = this.authService.registerUser(registrationRequestDto);
        return ResponseEntity
                .created(UriComponentsBuilder.fromPath(ApiPaths.BASE_API)
                        .pathSegment(ApiPaths.USERS)
                        .pathSegment(String.valueOf(response.userId()))
                        .build().toUri()
                )
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> authenticate(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletRequest request) {
        LoginResponseDto response = authService.authenticateUser(loginRequestDto);
        if (response.getIsTfaRequired()) {
            return ResponseEntity.ok(response);
        }

        return buildLoginResponseWithSession(response, request);
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<LoginResponseDto> verifyTfa(@Valid @RequestBody TwoFactorLoginRequest loginRequestDto, HttpServletRequest request) {
        LoginResponseDto response = authService.authenticateWith2fa(loginRequestDto);

        return buildLoginResponseWithSession(response, request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refreshToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, ApiConstants.REFRESH_TOKEN_COOKIE);
        if (cookie == null) {
            log.warn("Refresh token missing in request from IP {}", request.getRemoteAddr());
            throw new UserSessionException("No refresh token found in request", ErrorCode.TOKEN_NOT_FOUND);
        }
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        RefreshTokenResult result = userSessionService.refreshToken(cookie.getValue(), userAgent);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.cookie())
                .body(new AccessTokenResponse(result.accessToken()));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam String code) {
        authService.verifyUser(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(UriComponentsBuilder.fromPath(ApiPaths.BASE_API)
                        .pathSegment(ApiPaths.AUTH)
                        .pathSegment(ApiPaths.LOGIN)
                        .queryParam("activated", "true")
                        .build().toUri()
                )
                .build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ResponseMessageDto> resendVerification(@Valid @RequestBody ResendVerificationRequestDto requestDto) {
        return ResponseEntity
                .ok()
                .body(authService.resendVerification(requestDto.email()));
    }

    private ResponseEntity<LoginResponseDto> buildLoginResponseWithSession(LoginResponseDto response, HttpServletRequest request) {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        UserSession session = userSessionService.createUserSession(response.getUserId(), userAgent);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, userSessionService.generateResponseCookie(session.getRawRefreshToken()).toString())
                .body(response);
    }
}
