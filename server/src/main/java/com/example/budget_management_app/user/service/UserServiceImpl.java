package com.example.budget_management_app.user.service;

import com.example.budget_management_app.account.service.AccountService;
import com.example.budget_management_app.auth.dto.RegistrationRequest;
import com.example.budget_management_app.category.service.CategoryService;
import com.example.budget_management_app.common.dto.ResponseMessage;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.UserStatusException;
import com.example.budget_management_app.common.exception.ValidationException;
import com.example.budget_management_app.recurring_transaction.service.RecurringTransactionService;
import com.example.budget_management_app.security.service.TwoFactorAuthenticationService;
import com.example.budget_management_app.session.domain.UserSession;
import com.example.budget_management_app.session.service.UserSessionService;
import com.example.budget_management_app.transaction.service.TransactionService;
import com.example.budget_management_app.user.dao.UserDao;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.domain.UserStatus;
import com.example.budget_management_app.user.dto.*;
import com.example.budget_management_app.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final TwoFactorAuthenticationService tfaService;
    private final PasswordEncoder encoder;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final UserMapper mapper;
    private final UserSessionService userSessionService;
    private final UserService self;
    private final RecurringTransactionService recurringTransactionService;
    private final TransactionService transactionService;

    public UserServiceImpl(
            UserDao userDao,
            TwoFactorAuthenticationService tfaService,
            PasswordEncoder encoder,
            @Lazy CategoryService categoryService,
            @Lazy AccountService accountService,
            @Lazy UserSessionService userSessionService,
            @Lazy UserService self,
            UserMapper mapper,
            RecurringTransactionService recurringTransactionService,
            TransactionService transactionService) {
        this.userDao = userDao;
        this.tfaService = tfaService;
        this.encoder = encoder;
        this.categoryService = categoryService;
        this.accountService = accountService;
        this.mapper = mapper;
        this.userSessionService = userSessionService;
        this.self = self;
        this.recurringTransactionService = recurringTransactionService;
        this.transactionService = transactionService;
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponse getUser(Long id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id: " + id + " not found", ErrorCode.NOT_FOUND));

        return mapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id: " + id + " not found", ErrorCode.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Transactional
    @Override
    public User createUser(RegistrationRequest dto) {
        passwordsComparison(null, dto.password(), dto.passwordConfirmation());
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
    public UserResponse updateUser(Long userId, UpdateUserRequest dto) {
        User user = self.getUserById(userId);

        validateUserIsActive(user);

        if (StringUtils.hasText(dto.name()) && !user.getName().equals(dto.name())) {
            user.setName(dto.name());
        }
        if (StringUtils.hasText(dto.surname()) && !user.getSurname().equals(dto.surname())) {
            user.setSurname(dto.surname());
        }

        log.info("The user: {} has modified their data", userId);
        return mapper.toUserResponse(userDao.update(user));
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
            return;
        }

        accountService.activateAllByUser(user.getId());
        recurringTransactionService.activateAllByUser(user.getId());

        user.setStatus(UserStatus.ACTIVE);
        userDao.update(user);
        log.info("The user {} has been activated", email);
    }

    @Transactional
    @Override
    public ResponseMessage closeUser(Long userId) {
        User user = self.getUserById(userId);

        validateUserIsActive(user);

        accountService.deactivateAllByUser(userId);
        recurringTransactionService.deactivateAllByUser(userId);

        user.setRequestCloseAt(Instant.now());
        user.setStatus(UserStatus.PENDING_DELETION);
        userDao.update(user);

        log.info("The user: {} has closed the account", userId);
        return new ResponseMessage(
                "Your account will be closed within 30 days. " +
                        "If you change your mind and want to stop the process of deleting your account, " +
                        "simply log in to your account before the 30 days are up."
        );
    }

    @Transactional
    @Override
    public void deleteUsersPendingDeletion() {
        Instant cutoffDate = Instant.now().minus(30, ChronoUnit.DAYS);
        List<User> usersToDelete = userDao.findUsersForDeletion(UserStatus.PENDING_DELETION, cutoffDate);

        if (usersToDelete.isEmpty()) {
            log.info("No users found to delete");
            return;
        }

        log.info("Found {} users to permanent delete.", usersToDelete.size());

        for (User user : usersToDelete) {
            try {
                deleteUser(user.getId());
                log.info("Successfully delete user: {}", user.getId());

            } catch (Exception e) {
                log.error("Failed to delete user: {}. Error: {}", user.getId(), e.getMessage());
            }
        }
    }

    @Transactional
    @Override
    public void changePassword(Long userId, ChangePasswordRequest dto) {
        User user = self.getUserById(userId);

        validateUserIsActive(user);

        if (!encoder.matches(dto.oldPassword(), user.getPassword())) {
            log.warn("The user: {} provided incorrect old passwords", userId);
            throw new ValidationException("Incorrect old password", ErrorCode.INVALID_OLD_PASSWORD);
        }

        passwordsComparison(userId, dto.newPassword(), dto.passwordConfirmation());

        user.setPassword(encoder.encode(dto.newPassword()));
        userDao.update(user);
        log.info("The user: {} has changed password", userId);
    }

    @Override
    public void updateUserPassword(Long userId, String newPassword) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> {
                    log.error("Failed to find user by id {} during password reset, but token was valid.", userId);
                    return new NotFoundException(User.class.getSimpleName(), "id", userId, ErrorCode.USER_NOT_FOUND);
                });

        user.setPassword(encoder.encode(newPassword));

        userDao.update(user);
        log.info("Password has been reset for user: {}", userId);
    }

    @Transactional
    @Override
    public TfaQRCode tfaSetup(Long userId) {
        User user = self.getUserById(userId);

        validateUserIsActive(user);

        String secret = tfaService.generateSecret();
        user.setTempSecret(secret);
        userDao.update(user);

        log.info("The user: {}, setup tfa", userId);
        return new TfaQRCode(tfaService.generateQrCodeImageUri(secret));
    }

    @Transactional
    @Override
    public void verifyTfaSetup(Long userId, String code) {
        User user = self.getUserById(userId);

        validateUserIsActive(user);
        validateTfaCode(userId, user.getTempSecret(), code);

        user.setSecret(user.getTempSecret());
        user.setTempSecret(null);
        user.setMfaEnabled(true);
        userDao.update(user);
    }

    @Transactional
    @Override
    public void tfaDisable(Long userId, String code) {
        User user = self.getUserById(userId);

        validateUserIsActive(user);
        validateTfaCode(userId, user.getSecret(), code);

        user.setSecret(null);
        user.setMfaEnabled(false);
        userDao.update(user);
        log.info("The user: {}, turned off tfa", userId);
    }

    @Override
    public List<UserSessionResponse> getUserSessions(Long userId, Long currentSessionId) {
        List<UserSession> sessions = userSessionService.findSessionsByUser(userId);
        return sessions.stream().map(session -> mapper.toUserSessionResponse(session, currentSessionId)).toList();
    }

    @Override
    public void logoutSession(Long userId, Long sessionId) {
        userSessionService.logout(sessionId, userId);
    }

    private void deleteUser(Long userId) {
        User user = self.getUserById(userId);

        if (!user.getStatus().equals(UserStatus.PENDING_DELETION)) {
            log.error("Attempt to delete user {}, who is not awaiting deletion. Status: {}", userId, user.getStatus().name());
            throw new ValidationException(
                    "The user is not marked for deletion.",
                    ErrorCode.USER_NOT_PENDING_DELETION
            );
        }

        recurringTransactionService.deleteAllByUser(userId);
        transactionService.deleteAllByUser(userId);
        accountService.deleteAllByUser(userId);
        categoryService.deleteAllByUser(userId);

        userDao.delete(user);
        log.info("User with ID: {} has been successfully deleted.", userId);
    }

    private void passwordsComparison(Long userId, String password, String passwordConfirmation) {
        if (!password.equals(passwordConfirmation)) {
            log.warn("The user: {} provided different passwords", userId);
            throw new ValidationException("The provided passwords are different", ErrorCode.PASSWORDS_NOT_MATCH);
        }
    }

    private void validateUserIsActive(User user) {
        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new UserStatusException("User must be active to perform this action", ErrorCode.USER_NOT_ACTIVE);
        }
    }

    private void validateEmailUniqueness(String email) {
        if (userDao.findByEmail(email).isPresent()) {
            throw new ValidationException("This email: " + email + " is already used", ErrorCode.EMAIL_ALREADY_USED);
        }
    }

    private void validateTfaCode(Long userId, String userSecret, String code) {
        if (!tfaService.isOptValid(userSecret, code)) {
            log.warn("User: {}, delivered invalid tfa code. Failed setup.", userId);
            throw new ValidationException("Invalid 2FA code provided", ErrorCode.INVALID_CODE);
        }
    }

}