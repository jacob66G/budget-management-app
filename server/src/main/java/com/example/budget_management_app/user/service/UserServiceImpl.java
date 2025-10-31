package com.example.budget_management_app.user.service;

import com.example.budget_management_app.account.service.AccountService;
import com.example.budget_management_app.auth.dto.RegistrationRequestDto;
import com.example.budget_management_app.category.service.CategoryService;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.TfaException;
import com.example.budget_management_app.common.exception.ValidationException;
import com.example.budget_management_app.security.service.TwoFactorAuthenticationService;
import com.example.budget_management_app.user.dao.UserDao;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.domain.UserStatus;
import com.example.budget_management_app.user.dto.TfaQRCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final TwoFactorAuthenticationService tfaService;
    private final PasswordEncoder encoder;
    private final CategoryService categoryService;
    private final AccountService accountService;

    public UserServiceImpl(
            UserDao userDao,
            TwoFactorAuthenticationService tfaService,
            PasswordEncoder encoder,
            @Lazy CategoryService categoryService,
            @Lazy AccountService accountService
    ) {
        this.userDao = userDao;
        this.tfaService = tfaService;
        this.encoder = encoder;
        this.categoryService = categoryService;
        this.accountService = accountService;
    }

    @Override
    public User getUserById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User with id: " + id + " not found"));
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Transactional
    @Override
    public User createUser(RegistrationRequestDto dto) {
        validateEmailUniqueness(dto.email());

        User user = new User();
        user.setName(dto.name());
        user.setSurname(dto.surname());
        user.setEmail(dto.email());
        user.setPassword(encoder.encode(dto.password()));
        user.setStatus(UserStatus.PENDING_CONFIRMATION);
        user.setCreatedAt(Instant.now());
        categoryService.assignInitialCategories(user);
        accountService.createDefaultAccount(user);

        return userDao.save(user);
    }

    @Transactional
    @Override
    public void activateUser(String email) {
        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        User.class.getSimpleName(),
                        "email",
                        email,
                        ErrorCode.USER_NOT_FOUND
                ));

        if (user.getStatus() == UserStatus.ACTIVE) {
            log.info("User {} is already active", email);
            return;
        }

        user.setStatus(UserStatus.ACTIVE);
        userDao.save(user);
        log.info("User {} has been activated", email);
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

    private void validateEmailUniqueness(String email) {
        if (userDao.findByEmail(email).isPresent()) {
            throw new ValidationException("This email: " + email + " is already used", ErrorCode.EMAIL_ALREADY_USED);
        }
    }

    private void validateTfaCode(Long userId, String userSecret, String code) {
        if (!tfaService.isOptValid(userSecret, code)) {
            log.warn("User: {}, delivered invalid tfa code. Failed setup.", userId);
            throw new TfaException("Invalid code", ErrorCode.INVALID_CODE);
        }
    }

}