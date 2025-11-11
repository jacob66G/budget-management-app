package com.example.budget_management_app.auth.service;

import com.example.budget_management_app.auth.dto.*;
import com.example.budget_management_app.common.dto.ResponseMessageDto;

public interface AuthService {

    ResponseMessageDto registerUser(RegistrationRequestDto dto);

    LoginResponseDto authenticateUser(LoginRequestDto dto);

    LoginResponseDto authenticateWith2fa(TwoFactorLoginRequest dto);

    void verifyUser(String code);

    ResponseMessageDto resendVerification(String email);

    void resetPasswordConfirm(PasswordResetConfirmationDto dto);

    ResponseMessageDto resetPassword(PasswordResetRequestDto dto);

}
