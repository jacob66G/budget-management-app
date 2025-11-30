package com.example.budget_management_app.user.service;

import com.example.budget_management_app.auth.dto.RegistrationRequest;
import com.example.budget_management_app.common.dto.ResponseMessage;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.dto.*;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserResponse getUser(Long id);

    User getUserById(Long id);

    Optional<User> findUserByEmail(String email);

    User createUser(RegistrationRequest dto);

    UserResponse updateUser(Long userId, UpdateUserRequest dto);

    void activateUser(String email);

    ResponseMessage closeUser(Long userId);

    void deleteUsersPendingDeletion();

    void changePassword(Long userId, ChangePasswordRequest dto);

    void updateUserPassword(Long userId, String newPassword);

    TfaQRCode tfaSetup(Long userId);

    void verifyTfaSetup(Long userId, String code);

    void tfaDisable(Long userId, String code);

    List<UserSessionResponse> getUserSessions(Long userId, Long currentSessionId);

    void logoutSession(Long userId, Long sessionId);
}
