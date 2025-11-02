package com.example.budget_management_app.user.service;

import com.example.budget_management_app.auth.dto.RegistrationRequestDto;
import com.example.budget_management_app.common.dto.ResponseMessageDto;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.dto.ChangePasswordRequestDto;
import com.example.budget_management_app.user.dto.TfaQRCode;
import com.example.budget_management_app.user.dto.UpdateUserRequestDto;
import com.example.budget_management_app.user.dto.UserResponseDto;

import java.util.Optional;

public interface UserService {

    UserResponseDto getUser(Long id);

    User getUserById(Long id);

    Optional<User> findUserById(Long id);

    Optional<User> findUserByEmail(String email);

    User createUser(RegistrationRequestDto dto);

    UserResponseDto updateUser(Long userId, UpdateUserRequestDto dto);

    void activateUser(String email);

    ResponseMessageDto closeUser(Long userId);

    void deleteUsersPendingDeletion();

    void changePassword(Long userId, ChangePasswordRequestDto dto);

    void updateUserPassword(Long userId, String newPassword);

    TfaQRCode tfaSetup(Long userId);

    void verifyTfaSetup(Long userId, String code);

    void tfaDisable(Long userId, String code);
}
