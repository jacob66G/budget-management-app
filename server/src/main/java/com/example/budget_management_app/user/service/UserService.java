package com.example.budget_management_app.user.service;

import com.example.budget_management_app.auth.dto.RegistrationRequestDto;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.dto.TfaQRCode;

import java.util.Optional;

public interface UserService {

    User getUserById(Long id);

    Optional<User> findUserById(Long id);

    Optional<User> findUserByEmail(String email);

    User createUser(RegistrationRequestDto dto);

    void activateUser(String email);

    TfaQRCode tfaSetup(Long userId);

    void verifyTfaSetup(Long userId, String code);

    void tfaDisable(Long userId, String code);
}
