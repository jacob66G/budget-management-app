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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${SECRET_HEADER}")
    private String secretHeaderValue;
    private final AuthService authService;
    private final UserSessionService userSessionService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDto> registerUser(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {
        RegistrationResponseDto userResponse = this.authService.registerUser(registrationRequestDto);
        return ResponseEntity
                .created(UriComponentsBuilder.fromPath(ApiPaths.BASE_API)
                        .pathSegment(ApiPaths.USERS)
                        .pathSegment(String.valueOf(userResponse.userId()))
                        .build().toUri()
                )
                .body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> authenticate(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletRequest request) {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        LoginResponseDto responseDto = authService.authenticateUser(loginRequestDto);
        UserSession session = userSessionService.createUserSession(responseDto.userId(), userAgent);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, userSessionService.generateResponseCookie(session.getRawRefreshToken()).toString())
                .body(responseDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refreshToken(@RequestHeader(value = "X-Requested-With", required = false) String requestedWith, HttpServletRequest request) {
        if (!secretHeaderValue.equals(requestedWith)) {
            log.warn("Custom header missing in refresh request from IP {}", request.getRemoteAddr());
            throw new UserSessionException("No custom header found in refresh request", ErrorCode.CUSTOM_HEADER_NOT_FOUND);
        }

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
}
