package com.example.budget_management_app.auth.service;

import com.example.budget_management_app.auth.dto.*;
import com.example.budget_management_app.common.dto.ResponseMessageDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    ResponseMessageDto registerUser(RegistrationRequestDto dto);

    LoginResult authenticateUser(LoginRequestDto dto, HttpServletRequest request, String oldRefreshToken);

    LoginResult authenticateWith2fa(TwoFactorLoginRequest dto, HttpServletRequest request, String oldRefreshToken);

    void verifyUser(String code);

    ResponseMessageDto resendVerification(String email);

    void resetPasswordConfirm(PasswordResetConfirmationDto dto);

    ResponseMessageDto resetPassword(PasswordResetRequestDto dto);

}
