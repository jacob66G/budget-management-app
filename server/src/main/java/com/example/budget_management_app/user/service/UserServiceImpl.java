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
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final TwoFactorAuthenticationService tfaService;

    @Override
    public User getUserById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User with id: " + id + " not found"));
    }

    @Transactional
    @Override
    public TfaQRCode tfaSetup(Long userId) {
        User user = getUserById(userId);

        String secret = tfaService.generateSecret();
        user.setTempSecret(secret);
        userDao.update(user);

        log.info("User: {}, setup tfa", userId);
        return new TfaQRCode(tfaService.generateQrCodeImageUri(secret));
    }

    @Transactional
    @Override
    public void verifyTfaSetup(Long userId, String code) {
        User user = getUserById(userId);

        validateTfaCode(userId, user.getTempSecret(), code);

        user.setSecret(user.getTempSecret());
        user.setTempSecret(null);
        user.setMfaEnabled(true);
        userDao.update(user);
    }

    @Transactional
    @Override
    public void tfaDisable(Long userId, String code) {
        User user = getUserById(userId);

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