package com.example.budget_management_app.user.service;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.TfaException;
import com.example.budget_management_app.security.service.TwoFactorAuthenticationService;
import com.example.budget_management_app.user.dao.UserDao;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.dto.TfaQRCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final TwoFactorAuthenticationService tfaService;

    @Transactional
    public TfaQRCode tfaSetup(Long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User with id: " + userId + " not found"));

        String secret = tfaService.generateSecret();
        user.setTempSecret(secret);
        userDao.update(user);

        log.info("User: {}, setup tfa", userId);
        return new TfaQRCode(tfaService.generateQrCodeImageUri(secret));
    }

    @Transactional
    public void verifyTfaSetup(Long userId, String code) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User with id: " + userId + " not found."));

        validateTfaCode(userId, user.getTempSecret(), code);

        user.setSecret(user.getTempSecret());
        user.setTempSecret(null);
        user.setMfaEnabled(true);
        userDao.update(user);
    }

    @Transactional
    public void tfaDisable(Long userId, String code) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User with id: " + userId + " not found."));

        validateTfaCode(userId, user.getSecret(), code);

        user.setSecret(null);
        user.setMfaEnabled(false);
        userDao.update(user);
    }

    private void validateTfaCode(Long userId, String userSecret, String code) {
        if (!tfaService.isOptValid(userSecret, code)) {
            log.warn("User: {}, delivered invalid tfa code. Failed setup.", userId);
            throw new TfaException("Invalid code", ErrorCode.INVALID_CODE);
        }
    }

}