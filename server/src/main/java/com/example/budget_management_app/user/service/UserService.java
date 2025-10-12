package com.example.budget_management_app.user.service;

import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.dto.TfaQRCode;

public interface UserService {

    User getUserById(Long id);

    TfaQRCode tfaSetup(Long userId);

    void verifyTfaSetup(Long userId, String code);

    void tfaDisable(Long userId, String code);
}
