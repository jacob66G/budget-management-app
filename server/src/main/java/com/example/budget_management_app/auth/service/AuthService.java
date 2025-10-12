package com.example.budget_management_app.auth.service;

import com.example.budget_management_app.auth.dto.*;
import com.example.budget_management_app.common.dto.ResponseMessageDto;

public interface AuthService {

    RegistrationResponseDto registerUser(RegistrationRequestDto requestDto);

    LoginResponseDto authenticateUser(LoginRequestDto loginRequest);

    LoginResponseDto authenticateWith2fa(TwoFactorLoginRequest loginRequest);

    void verifyUser(String email, String verificationCode);

    ResponseMessageDto resendVerification(String email);

}
