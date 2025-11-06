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
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.service.UserService;
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
    private final UserService userService;
    private final StorageService storageService;
    private final IconKeyValidator iconKeyValidator;

    @Override
    public AccountDetailsResponseDto getAccount(Long userId, Long accountId) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));
        return mapper.toDetailsResponseDto(account);
    }

    @Override
    public List<AccountResponseDto> getAccounts(Long userId, SearchCriteria criteria) {
        List<Account> accounts = accountDao.findByUserAndCriteria(userId, criteria);
        return accounts.stream().map(mapper::toResponseDto).toList();
    }

    @Transactional
    @Override
    public AccountDetailsResponseDto createAccount(Long userId, AccountCreateRequestDto dto) {
        User user = userService.getUserById(userId);
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
        account.setIconKey(dto.iconKey());
        account.setAccountStatus(AccountStatus.ACTIVE);

        user.addAccount(account);
        Account savedAccount = accountDao.save(account);

        log.info("User: {} created new account: {}.", userId, savedAccount.getId());
        return mapper.toDetailsResponseDto(savedAccount);
    }

    @Transactional
    @Override
    public AccountDetailsResponseDto updateAccount(Long userId, Long accountId, AccountUpdateRequestDto dto) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));

        validateAccountIsActive(account);

        if (StringUtils.hasText(dto.name()) && !account.getName().equals(dto.name())) {
            validateNameUniqueness(userId, dto.name(), accountId);
            account.setName(dto.name());
        }
        if (StringUtils.hasText(dto.currency()) && !account.getCurrency().name().equals(dto.currency().toUpperCase())) {
            validateCurrency(dto.currency());
            validateCurrencyConsistency(accountId, dto.currency());

            account.setCurrency(SupportedCurrency.valueOf(dto.currency().toUpperCase()));
        }
        if (StringUtils.hasText(dto.description()) && !account.getDescription().equals(dto.description())) {
            account.setDescription(dto.description());
        }
        if (dto.initialBalance() != null && (account.getBalance() == null || account.getBalance().compareTo(dto.initialBalance()) != 0)) {
            //TODO check is account has transactions
            account.setBalance(dto.initialBalance());
        }
        if (dto.includeInTotalBalance() != null) {
            account.setIncludeInTotalBalance(dto.includeInTotalBalance());
        }
        if (dto.iconKey() != null) {
            validateIcon(dto.iconKey());
            account.setIconKey(dto.iconKey());
        }

        changeBudget(account, dto);

        log.info("User: {} modified account: {}.", userId, accountId);
        return mapper.toDetailsResponseDto(accountDao.update(account));
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
    public void activateAccount(Long userId, Long accountId) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));

        if (AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            return;
        }

        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setIncludeInTotalBalance(true);
        // TODO activate recurring transactions - recurringTransactionService.activateAllTransactions(accountId)

        log.info("User: {} activated account: {}.", userId, accountId);
        accountDao.update(account);
    }

    @Transactional
    @Override
    public void deactivateAccount(Long userId, Long accountId) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));

        if (AccountStatus.INACTIVE.equals(account.getAccountStatus())) {
            return;
        }

        account.setAccountStatus(AccountStatus.INACTIVE);
        account.setIncludeInTotalBalance(false);
        // TODO deactivate recurring transactions - recurringTransactionService.deactivateAllTransactions(accountId)

        log.info("User: {} deactivated account: {}.", userId, accountId);
        accountDao.update(account);
    }

    @Transactional
    @Override
    public void activateAllUserAccounts(Long userId) {
        accountDao.activateAll(userId);
        log.info("Activated all accounts for user {}", userId);
    }

    @Transactional
    @Override
    public void deactivateAllUserAccounts(Long userId) {
        accountDao.deactivateAll(userId);
        log.info("Deactivated all accounts for user {}", userId);
    }

    @Transactional
    @Override
    public void deleteAccount(Long userId, Long accountId) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));

        validateAccountCanBeDeleted(account);

        //TODO transasctionService.deleteAllByAccount(accountId);
        accountDao.delete(account);
        log.info("User: {} has removed account: {}.", userId, accountId);
    }

    @Transactional
    @Override
    public void deleteAllUserAccounts(Long userId) {
        accountDao.deleteAll(userId);
    }

    private void changeBudget(Account account, AccountUpdateRequestDto dto) {
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

        // Update the budget value if provided
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

        // Validate the consistency of budget type, budget value, and alert threshold
        validateBudgetAlertRelation(account.getBudgetType(), account.getBudget(), account.getAlertThreshold());

        // If the final budget type is NONE, ensure all related values are cleared
        if (account.getBudgetType() == BudgetType.NONE) {
            account.setBudget(null);
            account.setAlertThreshold(null);
        }
    }

    private void validateAccount(Long userId, AccountCreateRequestDto dto) {
        validateAccountType(dto.type());
        validateCurrency(dto.currency());
        validateBudgetType(dto.budgetType());

        BudgetType budgetType = BudgetType.valueOf(dto.budgetType().toUpperCase());
        validateBudgetAlertRelation(budgetType, dto.budget(), dto.alertThreshold());
        validateIcon(dto.iconKey());
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

    private void validateCurrencyConsistency(Long accountId, String currency) {
        //TODO check if exists transactions with other currency

    }

    private void validateIcon(String path) {
        if (path == null) {
            return;
        }

        if (!iconKeyValidator.isValidAccountIconKey(path)) {
            log.warn("Invalid icon key: {}.", path);
            throw new ValidationException("Selected icon is not valid.", ErrorCode.INVALID_RESOURCE_PATH);
        }
        if (!storageService.exists(path)) {
            log.warn("Resource with path: {} does not exists in storage", path);
            throw new NotFoundException("This image does not exist", ErrorCode.NOT_FOUND);
        }
    }

    private void validateAccountIsActive(Account account) {
        if (!account.getAccountStatus().equals(AccountStatus.ACTIVE)) {
            throw new ValidationException("You cannot modify an inactive account.", ErrorCode.INACTIVE_ACCOUNT);
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
