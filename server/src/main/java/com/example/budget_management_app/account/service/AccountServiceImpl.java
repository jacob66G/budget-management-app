package com.example.budget_management_app.account.service;

import com.example.budget_management_app.account.dao.AccountDao;
import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.account.domain.AccountStatus;
import com.example.budget_management_app.account.domain.AccountType;
import com.example.budget_management_app.account.domain.BudgetType;
import com.example.budget_management_app.account.dto.*;
import com.example.budget_management_app.account.mapper.AccountMapper;
import com.example.budget_management_app.common.enums.SupportedCurrency;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.ValidationException;
import com.example.budget_management_app.common.service.IconKeyValidator;
import com.example.budget_management_app.common.service.StorageService;
import com.example.budget_management_app.recurring_transaction.service.RecurringTransactionService;
import com.example.budget_management_app.transaction.service.TransactionService;
import com.example.budget_management_app.user.dao.UserDao;
import com.example.budget_management_app.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountDao accountDao;
    private final AccountMapper mapper;
    private final UserDao userDao;
    private final StorageService storageService;
    private final IconKeyValidator iconKeyValidator;
    private final TransactionService transactionService;
    private final RecurringTransactionService recurringTransactionService;

    @Transactional(readOnly = true)
    @Override
    public AccountDetailsResponse getAccount(Long userId, Long accountId) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));

        boolean hasTx = transactionService.existsByAccountAndUser(accountId, userId);
        return mapper.toDetailsResponse(account, hasTx);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AccountResponse> getAccounts(Long userId, SearchCriteria criteria) {
        List<Account> accounts = accountDao.findByUserAndCriteria(userId, criteria);
        return accounts.stream().map(mapper::toResponse).toList();
    }

    @Transactional
    @Override
    public AccountDetailsResponse createAccount(Long userId, AccountCreateRequest dto) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id: " + userId + " not found", ErrorCode.NOT_FOUND));
        validateAccount(userId, dto);

        Account account = new Account();
        account.setAccountType(AccountType.valueOf(dto.type().toUpperCase()));
        account.setName(dto.name());
        account.setCurrency(SupportedCurrency.valueOf(dto.currency().toUpperCase()));
        account.setDescription(dto.description());
        account.setBalance(dto.initialBalance() != null ? dto.initialBalance() : BigDecimal.ZERO);

        BudgetType budgetType = BudgetType.valueOf(dto.budgetType().toUpperCase());
        account.setBudgetType(budgetType);
        account.setBudget(dto.budget());
        account.setAlertThreshold(dto.alertThreshold());
        account.setCreatedAt(Instant.now());
        account.setIncludeInTotalBalance(dto.includeInTotalBalance());

        String iconKey = storageService.extractKey(dto.iconPath());
        validateIconKey(iconKey);
        account.setIconKey(iconKey);

        account.setAccountStatus(AccountStatus.ACTIVE);

        user.addAccount(account);
        Account savedAccount = accountDao.save(account);

        log.info("User: {} created new account: {}.", userId, savedAccount.getId());
        return mapper.toDetailsResponse(savedAccount, false);
    }

    @Transactional
    @Override
    public AccountDetailsResponse updateAccount(Long userId, Long accountId, AccountUpdateRequest dto) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));

        validateAccountIsActive(account);

        if (StringUtils.hasText(dto.name()) && !account.getName().equals(dto.name())) {
            validateNameUniqueness(userId, dto.name(), accountId);
            account.setName(dto.name());
        }
        if (StringUtils.hasText(dto.currency()) && !account.getCurrency().name().equalsIgnoreCase(dto.currency())) {
            validateCurrency(dto.currency());
            account.setCurrency(SupportedCurrency.valueOf(dto.currency().toUpperCase()));
        }
        if (StringUtils.hasText(dto.description()) && !account.getDescription().equals(dto.description())) {
            account.setDescription(dto.description());
        }
        if (dto.initialBalance() != null && (account.getBalance() == null || account.getBalance().compareTo(dto.initialBalance()) != 0)) {
            validateBalanceCanBeChanged(accountId, userId);
            account.setBalance(dto.initialBalance());
        }
        if (dto.includeInTotalBalance() != null) {
            account.setIncludeInTotalBalance(dto.includeInTotalBalance());
        }
        if (dto.iconPath() != null) {
            String iconKey = storageService.extractKey(dto.iconPath());
            validateIconKey(iconKey);
            account.setIconKey(iconKey);
        }

        changeBudget(account, dto);
        boolean hasTx = transactionService.existsByAccountAndUser(accountId, userId);

        log.info("User: {} modified account: {}.", userId, accountId);

        return mapper.toDetailsResponse(accountDao.update(account), hasTx);
    }

    @Override
    public void createDefaultAccount(User user) {
        Account account = new Account();
        account.setName("Main account");
        account.setAccountType(AccountType.PERSONAL);
        account.setCurrency(SupportedCurrency.PLN);
        account.setDefault(true);
        account.setBudgetType(BudgetType.NONE);
        account.setIconKey("accounts/bank-finance-loan-icon.png");
        account.setCreatedAt(Instant.now());
        account.setAccountStatus(AccountStatus.ACTIVE);

        user.addAccount(account);
    }

    @Transactional
    @Override
    public AccountDetailsResponse activateAccount(Long userId, Long accountId) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));
        boolean hasTx = transactionService.existsByAccountAndUser(accountId, userId);

        if (AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            log.info("User: {} attempted to activate account: {} which is already active.", userId, accountId);
            return mapper.toDetailsResponse(account, hasTx);
        }

        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setIncludeInTotalBalance(true);

        recurringTransactionService.activateAllByAccount(accountId, userId);

        log.info("User: {} activated account: {}.", userId, accountId);
        return mapper.toDetailsResponse(accountDao.update(account), hasTx);
    }

    @Transactional
    @Override
    public AccountDetailsResponse deactivateAccount(Long userId, Long accountId) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));
        boolean hasTx = transactionService.existsByAccountAndUser(accountId, userId);

        if (AccountStatus.INACTIVE.equals(account.getAccountStatus())) {
            log.info("User: {} attempted to deactivate account: {} which is already deactivated", userId, accountId);
            return mapper.toDetailsResponse(account, hasTx);
        }

        account.setAccountStatus(AccountStatus.INACTIVE);
        account.setIncludeInTotalBalance(false);

        recurringTransactionService.deactivateAllByAccount(accountId, userId);

        log.info("User: {} deactivated account: {}.", userId, accountId);
        return mapper.toDetailsResponse(accountDao.update(account), hasTx);
    }

    @Transactional
    @Override
    public void activateAllByUser(Long userId) {
        accountDao.activateAll(userId);
        log.info("Activated all accounts for user {}", userId);
    }

    @Transactional
    @Override
    public void deactivateAllByUser(Long userId) {
        accountDao.deactivateAll(userId);
        log.info("Deactivated all accounts for user {}", userId);
    }

    @Transactional
    @Override
    public void deleteAccount(Long userId, Long accountId) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));

        validateAccountCanBeDeleted(account);

        recurringTransactionService.deleteAllByAccount(accountId, userId);
        transactionService.deleteAllByAccount(accountId, userId);
        accountDao.delete(account);
        log.info("User: {} has deleted account: {}.", userId, accountId);
    }

    @Transactional
    @Override
    public void deleteAllByUser(Long userId) {
        accountDao.deleteAll(userId);
    }

    private void changeBudget(Account account, AccountUpdateRequest dto) {
        BudgetType currentType = account.getBudgetType() == null ? BudgetType.NONE : account.getBudgetType();

        BudgetType newType = null;
        if (dto.budgetType() != null) {
            validateBudgetType(dto.budgetType());
            newType = BudgetType.valueOf(dto.budgetType().toUpperCase());
        }

        // No changes – nothing to update
        if (newType == null && dto.budget() == null && dto.alertThreshold() == null && currentType == BudgetType.NONE) {
            return;
        }

        // If the user sets the budget type to NONE, clear all related fields
        if (newType == BudgetType.NONE) {
            account.setBudgetType(BudgetType.NONE);
            account.setBudget(null);
            account.setAlertThreshold(null);
            return;
        }

        BudgetType effectiveType = newType != null ? newType : currentType;

        // If the budget was previously disabled but is now being enabled,
        // both budget and alertThreshold must be provided
        if (currentType == BudgetType.NONE && effectiveType != BudgetType.NONE) {
            if (dto.budget() == null || dto.alertThreshold() == null) {
                throw new ValidationException(
                        "When enabling budget you must provide both budget and alertThreshold",
                        ErrorCode.INVALID_BUDGET_ALERT_RELATION
                );
            }
        }

        // Update the budget amount if provided
        if (dto.budget() != null) {
            account.setBudget(dto.budget());
        }

        // Update the alert threshold if provided
        if (dto.alertThreshold() != null) {
            account.setAlertThreshold(dto.alertThreshold());
        }

        // Update the budget type if a new one was specified
        if (newType != null) {
            account.setBudgetType(newType);
        }

        // Validate the consistency of budget type, budget amount, and alert threshold
        validateBudgetAlertRelation(account.getBudgetType(), account.getBudget(), account.getAlertThreshold());

        // If the final budget type is NONE, ensure all related values are cleared
        if (account.getBudgetType() == BudgetType.NONE) {
            account.setBudget(null);
            account.setAlertThreshold(null);
        }
    }

    private void validateAccount(Long userId, AccountCreateRequest dto) {
        validateAccountType(dto.type());
        validateCurrency(dto.currency());
        validateBudgetType(dto.budgetType());

        BudgetType budgetType = BudgetType.valueOf(dto.budgetType().toUpperCase());
        validateBudgetAlertRelation(budgetType, dto.budget(), dto.alertThreshold());
        validateNameUniqueness(userId, dto.name(), null);
    }

    private void validateNameUniqueness(Long userId, String name, Long existingAccountId) {
        if (accountDao.existsByNameAndUser(name, userId, existingAccountId)) {
            throw new ValidationException("You already have account with name: " + name, ErrorCode.NAME_ALREADY_USED);
        }
    }

    private void validateAccountType(String type) {
        try {
            AccountType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Incorrect account type provided: {}", type);
            throw new ValidationException("Incorrect type: " + type, ErrorCode.WRONG_TYPE);
        }
    }

    private void validateCurrency(String currency) {
        try {
            SupportedCurrency.valueOf(currency.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Incorrect currency provided: {}", currency);
            throw new ValidationException("Incorrect currency: " + currency, ErrorCode.WRONG_CURRENCY);
        }
    }

    private void validateBudgetType(String budgetType) {
        try {
            BudgetType.valueOf(budgetType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Incorrect budget type provided: {}", budgetType);
            throw new ValidationException("Incorrect budget type: " + budgetType, ErrorCode.WRONG_BUDGET_TYPE);
        }
    }

    private void validateIconKey(String key) {
        if (key == null) {
            return;
        }

        if (!iconKeyValidator.isValidAccountIconKey(key)) {
            log.warn("Invalid icon key: {}.", key);
            throw new ValidationException("Selected icon is not valid.", ErrorCode.INVALID_RESOURCE_PATH);
        }
    }

    private void validateAccountIsActive(Account account) {
        if (!account.getAccountStatus().equals(AccountStatus.ACTIVE)) {
            throw new ValidationException("You cannot modify an inactive account.", ErrorCode.INACTIVE_ACCOUNT);
        }
    }

    private void validateBalanceCanBeChanged(Long accountId, Long userId) {
        if (transactionService.existsByAccountAndUser(accountId, userId)) {
            throw new ValidationException("The budget cannot be changed because there are already transactions in the account.", ErrorCode.MODIFY_BUDGET_WITH_TRANSACTIONS);
        }
    }

    private void validateAccountCanBeDeleted(Account account) {
        if (account.isDefault()) {
            throw new ValidationException("Cannot delete default account", ErrorCode.DELETE_DEFAULT_ACCOUNT);
        }

        if (!account.getAccountStatus().equals(AccountStatus.INACTIVE)) {
            throw new ValidationException("Incorrect account status: " + account.getAccountStatus().name(), ErrorCode.DELETE_ACTIVE_ACCOUNT);
        }
    }

    private void validateBudgetAlertRelation(BudgetType budgetType, BigDecimal budget, Double alertThreshold) {
        boolean hasBudget = budget != null;
        boolean hasAlert = alertThreshold != null;

        if (budgetType == BudgetType.NONE) {
            if (hasBudget || hasAlert) {
                throw new ValidationException("If budget type is NONE, budget and alert threshold must be null",
                        ErrorCode.INVALID_BUDGET_ALERT_RELATION);
            }
            return;
        }

        if (!hasBudget && hasAlert) {
            throw new ValidationException("Alert threshold requires a defined budget",
                    ErrorCode.INVALID_BUDGET_ALERT_RELATION);
        }

        if (hasBudget && !hasAlert) {
            throw new ValidationException("Budget requires an alert threshold to be set",
                    ErrorCode.INVALID_BUDGET_ALERT_RELATION);
        }
    }

}
