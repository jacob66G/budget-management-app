package com.example.budget_management_app.account.service;

import com.example.budget_management_app.account.dao.AccountDao;
import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.account.domain.AccountStatus;
import com.example.budget_management_app.account.domain.AccountType;
import com.example.budget_management_app.account.domain.BudgetType;
import com.example.budget_management_app.account.dto.AccountCreateRequestDto;
import com.example.budget_management_app.account.dto.AccountDetailsResponseDto;
import com.example.budget_management_app.account.dto.AccountUpdateRequestDto;
import com.example.budget_management_app.account.mapper.AccountMapper;
import com.example.budget_management_app.common.enums.SupportedCurrency;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.ValidationException;
import com.example.budget_management_app.common.service.S3PathValidator;
import com.example.budget_management_app.common.service.StorageService;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountDao accountDao;
    @Mock
    private AccountMapper mapper;
    @Mock
    private UserService userService;
    @Mock
    private StorageService storageService;
    @Mock
    private S3PathValidator s3PathValidator;
    //    @Mock
//    private RecurringTransactionService recurringTransactionService;
//    @Mock
//    private TransactionService transactionService;
    @InjectMocks
    private AccountServiceImpl accountService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Nested
    class CreateAccountTests {

        private User user;
        private AccountCreateRequestDto requestDto;
        private Account savedAccount;
        private AccountDetailsResponseDto expectedResult;
        private Long userId = 1L;
        private String accountName = "main";
        private String iconPath = "https://budget-management-app-bucket-2025.s3.eu-north-1.amazonaws.com/accounts/test.png";
        private BigDecimal balance = BigDecimal.valueOf(1000);
        private String description = "my main account";
        private BigDecimal budget = BigDecimal.valueOf(500);
        private Double alertThreshold = 20.0;

        @BeforeEach
        void setUp() {
            user = new User();
            user.setId(userId);

            requestDto = new AccountCreateRequestDto(
                    "PERSONAL",
                    accountName,
                    "PLN",
                    description,
                    balance,
                    "MONTHLY",
                    budget,
                    alertThreshold,
                    true,
                    iconPath
            );

            savedAccount = new Account();
            savedAccount.setId(1L);
            savedAccount.setName(accountName);
            savedAccount.setBalance(balance);
            savedAccount.setTotalIncome(BigDecimal.ZERO);
            savedAccount.setTotalExpense(BigDecimal.ZERO);
            savedAccount.setAccountStatus(AccountStatus.ACTIVE);
            savedAccount.setAccountType(AccountType.PERSONAL);
            savedAccount.setCurrency(SupportedCurrency.PLN);
            savedAccount.setDefault(false);
            savedAccount.setDescription(description);
            savedAccount.setBudgetType(BudgetType.MONTHLY);
            savedAccount.setBudget(budget);
            savedAccount.setAlertThreshold(alertThreshold);
            savedAccount.setCreatedAt(Instant.now());
            savedAccount.setIconPath(iconPath);
            savedAccount.setIncludeInTotalBalance(true);
            savedAccount.setUser(user);

            expectedResult = new AccountDetailsResponseDto(
                    1L,
                    "PERSONAL",
                    accountName,
                    balance,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "PLN",
                    false,
                    description,
                    "MONTHLY",
                    budget,
                    alertThreshold,
                    iconPath,
                    true,
                    savedAccount.getCreatedAt(),
                    "ACTIVE"
            );
        }

        @Test
        void should_create_new_account() {
            //given
            when(userService.getUserById(userId)).thenReturn(user);
            when(accountDao.existsByNameAndUser(accountName, userId, null)).thenReturn(false);
            when(s3PathValidator.isValidPathForAccount(iconPath)).thenReturn(true);
            when(storageService.exists(iconPath)).thenReturn(true);
            when(accountDao.save(any(Account.class))).thenReturn(savedAccount);
            when(mapper.toDetailsResponseDto(savedAccount)).thenReturn(expectedResult);

            //When
            AccountDetailsResponseDto result = accountService.createAccount(userId, requestDto);

            //then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedResult);
            assertThat(result.name()).isEqualTo(accountName);

            verify(userService, times(1)).getUserById(userId);
            verify(accountDao, times(1)).existsByNameAndUser(accountName, userId, null);
            verify(s3PathValidator, times(1)).isValidPathForAccount(iconPath);
            verify(storageService, times(1)).exists(iconPath);
            verify(mapper, times(1)).toDetailsResponseDto(savedAccount);

            verify(accountDao, times(1)).save(accountCaptor.capture());
            Account accountSentToDao = accountCaptor.getValue();

            assertThat(accountSentToDao).isNotNull();
            assertThat(accountSentToDao.getName()).isEqualTo(requestDto.name());
            assertThat(accountSentToDao.getBalance()).isEqualTo(requestDto.initialBalance());
            assertThat(accountSentToDao.getCurrency().name()).isEqualTo(requestDto.currency());
            assertThat(accountSentToDao.getBudgetType().name()).isEqualTo(requestDto.budgetType());
            assertThat(accountSentToDao.getIconPath()).isEqualTo(requestDto.iconPath());
            assertThat(accountSentToDao.getUser()).isEqualTo(user);

            assertThat(accountSentToDao.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(accountSentToDao.isDefault()).isFalse();
            assertThat(accountSentToDao.getCreatedAt()).isNotNull();
        }

        @Test
        void should_throw_validationException_when_incorrect_account_type() {
            //given
            String accountWrongType = "no existent type";
            AccountCreateRequestDto requestWithWrongType = new AccountCreateRequestDto(
                    accountWrongType,
                    accountName,
                    "PLN",
                    description,
                    balance,
                    "MONTHLY",
                    budget,
                    alertThreshold,
                    true,
                    iconPath
            );

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithWrongType));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WRONG_TYPE);
            assertThat(exception.getMessage()).contains(accountWrongType);
            verify(userService, times(1)).getUserById(userId);
        }

        @Test
        void should_throw_validationException_when_incorrect_currency() {
            //given
            String wrongCurrency = "no existent currency";
            AccountCreateRequestDto requestWithWrongCurrency = new AccountCreateRequestDto(
                    "PERSONAL",
                    accountName,
                    wrongCurrency,
                    description,
                    balance,
                    "MONTHLY",
                    budget,
                    alertThreshold,
                    true,
                    iconPath
            );

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithWrongCurrency));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WRONG_CURRENCY);
            assertThat(exception.getMessage()).contains(wrongCurrency);
            verify(userService, times(1)).getUserById(userId);
        }

        @Test
        void should_throw_validationException_when_incorrect_budget_type() {
            //given
            String budgetWrongType = "no existent budget type";
            AccountCreateRequestDto requestWithWrongBudgetType = new AccountCreateRequestDto(
                    "PERSONAL",
                    accountName,
                    "PLN",
                    description,
                    balance,
                    budgetWrongType,
                    budget,
                    alertThreshold,
                    true,
                    iconPath
            );

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithWrongBudgetType));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WRONG_BUDGET_TYPE);
            assertThat(exception.getMessage()).contains(budgetWrongType);
            verify(userService, times(1)).getUserById(userId);
        }

        @Test
        void should_throw_validationException_when_user_have_account_with_the_same_name() {
            //given
            when(userService.getUserById(userId)).thenReturn(user);
            when(accountDao.existsByNameAndUser(accountName, userId, null)).thenReturn(true);
            when(s3PathValidator.isValidPathForAccount(iconPath)).thenReturn(true);
            when(storageService.exists(iconPath)).thenReturn(true);

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestDto));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NAME_ALREADY_USED);
            assertThat(exception.getMessage()).contains(accountName);

            verify(userService, times(1)).getUserById(userId);
            verify(accountDao, times(1)).existsByNameAndUser(accountName, userId, null);
            verify(s3PathValidator, times(1)).isValidPathForAccount(iconPath);
            verify(storageService, times(1)).exists(iconPath);
        }

        @Test
        void should_throw_validationException_when_budget_type_is_none_and_budget_is_not_null() {
            //given
            AccountCreateRequestDto requestWithBudget = new AccountCreateRequestDto(
                    "PERSONAL",
                    accountName,
                    "PLN",
                    description,
                    balance,
                    "NONE",
                    budget,
                    null,
                    true,
                    iconPath
            );
            when(userService.getUserById(userId)).thenReturn(user);

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithBudget));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_BUDGET_ALERT_RELATION);

            verify(userService, times(1)).getUserById(userId);
        }

        @Test
        void should_throw_validationException_when_budget_type_is_none_and_alertThreshold_is_not_null() {
            //given
            AccountCreateRequestDto requestWithBudget = new AccountCreateRequestDto(
                    "PERSONAL",
                    accountName,
                    "PLN",
                    description,
                    balance,
                    "NONE",
                    null,
                    alertThreshold,
                    true,
                    iconPath
            );
            when(userService.getUserById(userId)).thenReturn(user);

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithBudget));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_BUDGET_ALERT_RELATION);

            verify(userService, times(1)).getUserById(userId);
        }

        @Test
        void should_throw_validationException_when_budget_type_is_not_none_and_budget_is_null() {
            //given
            AccountCreateRequestDto requestWithBudget = new AccountCreateRequestDto(
                    "PERSONAL",
                    accountName,
                    "PLN",
                    description,
                    balance,
                    "WEEKLY",
                    null,
                    alertThreshold,
                    true,
                    iconPath
            );
            when(userService.getUserById(userId)).thenReturn(user);

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithBudget));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_BUDGET_ALERT_RELATION);

            verify(userService, times(1)).getUserById(userId);
        }

        @Test
        void should_throw_validationException_when_budget_type_is_not_none_and_alertThreshold_is_null() {
            //given
            AccountCreateRequestDto requestWithBudget = new AccountCreateRequestDto(
                    "PERSONAL",
                    accountName,
                    "PLN",
                    description,
                    balance,
                    "WEEKLY",
                    budget,
                    null,
                    true,
                    iconPath
            );
            when(userService.getUserById(userId)).thenReturn(user);

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithBudget));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_BUDGET_ALERT_RELATION);

            verify(userService, times(1)).getUserById(userId);
        }

        @Test
        void should_throw_validationException_when_icon_path_is_invalid() {
            //given
            when(userService.getUserById(userId)).thenReturn(user);
            when(s3PathValidator.isValidPathForAccount(iconPath)).thenReturn(false);

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestDto));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_RESOURCE_PATH);

            verify(userService, times(1)).getUserById(userId);
            verify(s3PathValidator, times(1)).isValidPathForAccount(iconPath);
        }

        @Test
        void should_throw_notFoundException_when_icon_path_no_exists() {
            //given
            when(userService.getUserById(userId)).thenReturn(user);
            when(s3PathValidator.isValidPathForAccount(iconPath)).thenReturn(true);
            when(storageService.exists(iconPath)).thenReturn(false);

            //when + then
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> accountService.createAccount(userId, requestDto));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);

            verify(userService, times(1)).getUserById(userId);
            verify(s3PathValidator, times(1)).isValidPathForAccount(iconPath);
            verify(storageService, times(1)).exists(iconPath);
        }
    }

    @Nested
    class UpdateAccountTests {

        private Account existingAccount;
        private Long accountId = 1L;
        private Long userId = 1L;
        private User user;

        @BeforeEach
        void setUp() {
            user = new User();
            user.setId(userId);

            existingAccount = new Account();
            existingAccount.setId(accountId);
            existingAccount.setName("old name");
            existingAccount.setAccountStatus(AccountStatus.ACTIVE);
            existingAccount.setCurrency(SupportedCurrency.PLN);
            existingAccount.setDescription("old des");
            existingAccount.setBudgetType(BudgetType.NONE);
            existingAccount.setBudget(null);
            existingAccount.setAlertThreshold(null);
            existingAccount.setUser(user);

            when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.of(existingAccount));
        }

        @Test
        void should_update_simple_fields_successfully() {
            //given
            String newName = "new name";
            String newDesc = "new des";
            String newIcon = "https://new.path/icon.png";

            AccountUpdateRequestDto dto = new AccountUpdateRequestDto(
                    newName,
                    null,
                    newDesc,
                    null,
                    null,
                    null,
                    null,
                    false,
                    newIcon
            );

            when(accountDao.existsByNameAndUser(newName, userId, accountId)).thenReturn(false);
            when(s3PathValidator.isValidPathForAccount(newIcon)).thenReturn(true);
            when(storageService.exists(newIcon)).thenReturn(true);

            when(accountDao.update(any(Account.class))).thenReturn(existingAccount);
            when(mapper.toDetailsResponseDto(existingAccount)).thenReturn(mock(AccountDetailsResponseDto.class));

            //when
            accountService.updateAccount(userId, accountId, dto);

            //then
            verify(accountDao, times(1)).update(accountCaptor.capture());
            verify(mapper, times(1)).toDetailsResponseDto(existingAccount);

            Account updatedAccount = accountCaptor.getValue();
            assertThat(updatedAccount.getName()).isEqualTo(newName);
            assertThat(updatedAccount.getDescription()).isEqualTo(newDesc);
            assertThat(updatedAccount.getIconPath()).isEqualTo(newIcon);
            assertThat(updatedAccount.isIncludeInTotalBalance()).isFalse();
        }

        @Test
        void should_throw_ValidationException_when_updating_inactive_account() {
            //given
            existingAccount.setAccountStatus(AccountStatus.INACTIVE);
            AccountUpdateRequestDto dto = new AccountUpdateRequestDto(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            //when & then
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> accountService.updateAccount(userId, accountId, dto));

            //then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INACTIVE_ACCOUNT);

            verify(accountDao, never()).update(any());
            verify(mapper, never()).toDetailsResponseDto(any());
        }

        @Test
        void should_throw_ValidationException_when_new_name_is_already_used() {
            //given
            String newName = "existing name";
            AccountUpdateRequestDto dto = new AccountUpdateRequestDto(
                    newName,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(accountDao.existsByNameAndUser(newName, userId, accountId)).thenReturn(true);

            //when & then
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> accountService.updateAccount(userId, accountId, dto));

            //then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NAME_ALREADY_USED);
            verify(accountDao, never()).update(any());
            verify(mapper, never()).toDetailsResponseDto(any());
        }

        @Test
        void should_update_currency_and_call_consistency_check() {
            //given
            String newCurrency = "USD";
            AccountUpdateRequestDto dto = new AccountUpdateRequestDto(
                    null,
                    newCurrency,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(accountDao.update(any(Account.class))).thenReturn(existingAccount);
            when(mapper.toDetailsResponseDto(existingAccount)).thenReturn(mock(AccountDetailsResponseDto.class));

            //when
            accountService.updateAccount(userId, accountId, dto);

            //then
            // TODO: Po implementacji validateCurrencyConsistency, dodaj verify() dla tej metody.

            verify(accountDao, times(1)).update(accountCaptor.capture());
            Account updatedAccount = accountCaptor.getValue();
            assertThat(updatedAccount.getCurrency()).isEqualTo(SupportedCurrency.USD);
        }

        @Test
        void should_update_initialBalance() {
            //given
            BigDecimal newBalance = BigDecimal.valueOf(999.00);
            AccountUpdateRequestDto dto = new AccountUpdateRequestDto(
                    null,
                    null,
                    null,
                    newBalance,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(accountDao.update(any(Account.class))).thenReturn(existingAccount);
            when(mapper.toDetailsResponseDto(existingAccount)).thenReturn(mock(AccountDetailsResponseDto.class));

            //when
            accountService.updateAccount(userId, accountId, dto);

            //then
            // TODO: Po implementacji logiki sprawdzania transakcji, rozbuduj ten test.

            verify(accountDao, times(1)).update(accountCaptor.capture());
            Account updatedAccount = accountCaptor.getValue();
            assertThat(updatedAccount.getBalance()).isEqualByComparingTo(newBalance);
        }

        // --- changeBudget tests ---

        @Test
        void changeBudget_should_clear_fields_when_type_is_set_to_NONE() {
            //given
            existingAccount.setBudgetType(BudgetType.MONTHLY);
            existingAccount.setBudget(BigDecimal.valueOf(1000));
            existingAccount.setAlertThreshold(50.0);

            AccountUpdateRequestDto dto = new AccountUpdateRequestDto(
                    null,
                    null,
                    null,
                    null,
                    "NONE",
                    null,
                    null,
                    null,
                    null
            );

            when(accountDao.update(any(Account.class))).thenReturn(existingAccount);
            when(mapper.toDetailsResponseDto(existingAccount)).thenReturn(mock(AccountDetailsResponseDto.class));

            //when
            accountService.updateAccount(userId, accountId, dto);

            //then
            verify(accountDao, times(1)).update(accountCaptor.capture());
            Account updatedAccount = accountCaptor.getValue();

            assertThat(updatedAccount.getBudgetType()).isEqualTo(BudgetType.NONE);
            assertThat(updatedAccount.getBudget()).isNull();
            assertThat(updatedAccount.getAlertThreshold()).isNull();
        }

        @Test
        void changeBudget_should_throw_when_enabling_budget_without_budget_value() {
            //given
            //existingAccount has BudgetType.NONE by default
            AccountUpdateRequestDto dto = new AccountUpdateRequestDto(
                    null,
                    null,
                    null,
                    null,
                    "MONTHLY",
                    null,
                    50.0,
                    null,
                    null
            );

            //when & then
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> accountService.updateAccount(userId, accountId, dto));

            //then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_BUDGET_ALERT_RELATION);
            verify(accountDao, never()).update(any());
        }

        @Test
        void changeBudget_should_throw_when_enabling_budget_without_alertThreshold() {
            //given
            //existingAccount has BudgetType.NONE by default
            AccountUpdateRequestDto dto = new AccountUpdateRequestDto(
                    null,
                    null,
                    null,
                    null,
                    "MONTHLY",
                    BigDecimal.valueOf(1000),
                    null,
                    null,
                    null
            );

            //when & then
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> accountService.updateAccount(userId, accountId, dto));

            //then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_BUDGET_ALERT_RELATION);
            verify(accountDao, never()).update(any());
        }

        @Test
        void changeBudget_should_successfully_enable_budget() {
            //given
            //existingAccount has BudgetType.NONE by default
            BigDecimal newBudget = BigDecimal.valueOf(1000);
            Double newAlert = 50.0;
            AccountUpdateRequestDto dto = new AccountUpdateRequestDto(
                    null,
                    null,
                    null,
                    null,
                    "MONTHLY",
                    newBudget,
                    newAlert,
                    null,
                    null
            );

            when(accountDao.update(any(Account.class))).thenReturn(existingAccount);
            when(mapper.toDetailsResponseDto(existingAccount)).thenReturn(mock(AccountDetailsResponseDto.class));

            //when
            accountService.updateAccount(userId, accountId, dto);

            //then
            verify(accountDao, times(1)).update(accountCaptor.capture());
            Account updatedAccount = accountCaptor.getValue();

            assertThat(updatedAccount.getBudgetType()).isEqualTo(BudgetType.MONTHLY);
            assertThat(updatedAccount.getBudget()).isEqualByComparingTo(newBudget);
            assertThat(updatedAccount.getAlertThreshold()).isEqualTo(newAlert);
        }

        @Test
        void changeBudget_should_update_only_budget_value_when_already_active() {
            //given
            existingAccount.setBudgetType(BudgetType.MONTHLY);
            existingAccount.setBudget(BigDecimal.valueOf(1000));
            existingAccount.setAlertThreshold(50.0);

            BigDecimal updatedBudget = BigDecimal.valueOf(1500);
            AccountUpdateRequestDto dto = new AccountUpdateRequestDto(
                    null,
                    null,
                    null,
                    null,
                    null,
                    updatedBudget,
                    null,
                    null,
                    null
            );

            when(accountDao.update(any(Account.class))).thenReturn(existingAccount);
            when(mapper.toDetailsResponseDto(existingAccount)).thenReturn(mock(AccountDetailsResponseDto.class));

            //when
            accountService.updateAccount(userId, accountId, dto);

            //then
            verify(accountDao, times(1)).update(accountCaptor.capture());
            Account updatedAccount = accountCaptor.getValue();

            assertThat(updatedAccount.getBudgetType()).isEqualTo(BudgetType.MONTHLY);
            assertThat(updatedAccount.getAlertThreshold()).isEqualTo(50.0);
            assertThat(updatedAccount.getBudget()).isEqualByComparingTo(updatedBudget);
        }
    }

    @Nested
    class ActivateAccountTests {

        private Account account;
        private Long accountId = 1L;
        private Long userId = 1L;

        @BeforeEach
        void setUp() {
            User user = new User();
            user.setId(userId);

            account = new Account();
            account.setId(accountId);
            account.setUser(user);
            account.setAccountStatus(AccountStatus.INACTIVE);
            account.setIncludeInTotalBalance(false);
        }

        @Test
        void should_activate_an_inactive_account() {
            //given
            // Status INACTIVE set in setUp
            when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.of(account));

            //when
            accountService.activateAccount(userId, accountId);

            //then
            verify(accountDao, times(1)).update(accountCaptor.capture());
            Account updatedAccount = accountCaptor.getValue();

            assertThat(updatedAccount.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(updatedAccount.isIncludeInTotalBalance()).isTrue();

            // TODO verify(recurringTransactionService, times(1)).activateAllTransactions(accountId);
        }

        @Test
        void should_do_nothing_if_account_is_already_active() {
            //given
            account.setAccountStatus(AccountStatus.ACTIVE);
            when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.of(account));

            //when
            accountService.activateAccount(userId, accountId);

            //then
            verify(accountDao, never()).update(any());

            // TODO: verify(recurringTransactionService, never()).activateAllTransactions(anyLong());
        }

        @Test
        void should_throw_NotFoundException_if_account_not_found() {
            //given
            when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.empty());

            //when & then
            assertThrows(NotFoundException.class,
                    () -> accountService.activateAccount(userId, accountId));

            verify(accountDao, never()).update(any());
        }
    }

    @Nested
    class DeactivateAccountTests {

        private Account account;
        private Long accountId = 1L;
        private Long userId = 1L;

        @BeforeEach
        void setUp() {
            User user = new User();
            user.setId(userId);

            account = new Account();
            account.setId(accountId);
            account.setUser(user);
            account.setAccountStatus(AccountStatus.ACTIVE);
            account.setIncludeInTotalBalance(true);
        }

        @Test
        void should_deactivate_an_active_account() {
            //given
            // Status ACTIVE set in setUp
            when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.of(account));

            //when
            accountService.deactivateAccount(userId, accountId);

            //then
            verify(accountDao, times(1)).update(accountCaptor.capture());
            Account updatedAccount = accountCaptor.getValue();

            assertThat(updatedAccount.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
            assertThat(updatedAccount.isIncludeInTotalBalance()).isFalse();

            // TODO: verify(recurringTransactionService, times(1)).deactivateAllTransactions(accountId);
        }

        @Test
        void should_do_nothing_if_account_is_already_inactive() {
            //given
            account.setAccountStatus(AccountStatus.INACTIVE);
            when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.of(account));

            //when
            accountService.deactivateAccount(userId, accountId);

            //then
            verify(accountDao, never()).update(any());

            // TODO: verify(recurringTransactionService, never()).deactivateAllTransactions(anyLong());
        }

        @Test
        void should_throw_NotFoundException_if_account_not_found() {
            //given
            when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.empty());

            //when & then
            assertThrows(NotFoundException.class,
                    () -> accountService.deactivateAccount(userId, accountId));

            verify(accountDao, never()).update(any());
        }
    }

    @Nested
    class DeleteAccountTests {

        private Account account;
        private Long accountId = 1L;
        private Long userId = 1L;

        @BeforeEach
        void setUp() {
            User user = new User();
            user.setId(userId);

            account = new Account();
            account.setId(accountId);
            account.setUser(user);
            account.setAccountStatus(AccountStatus.INACTIVE);
            account.setDefault(false);
        }

        @Test
        void should_delete_account_successfully() {
            //given
            // Status INACTIVE and not-default set w setUp
            when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.of(account));

            //when
            accountService.deleteAccount(userId, accountId);

            //then
            // TODO: verify(transactionService, times(1)).deleteAllByAccount(accountId);
            verify(accountDao, times(1)).delete(account);
        }

        @Test
        void should_throw_ValidationException_when_deleting_default_account() {
            //given
            account.setDefault(true);
            when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.of(account));

            //when & then
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> accountService.deleteAccount(userId, accountId));

            //then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DELETE_DEFAULT_ACCOUNT);
            verify(accountDao, never()).delete(any());
            // TODO: verify(transactionService, never()).deleteAllByAccount(anyLong());
        }

        @Test
        void should_throw_ValidationException_when_deleting_active_account() {
            //given
            account.setAccountStatus(AccountStatus.ACTIVE);
            when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.of(account));

            //when & then
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> accountService.deleteAccount(userId, accountId));

            //then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DELETE_ACTIVE_ACCOUNT);
            verify(accountDao, never()).delete(any());
            // TODO: verify(transactionService, never()).deleteAllByAccount(anyLong());
        }

        @Test
        void should_throw_NotFoundException_if_account_not_found() {
            //given
            when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.empty());

            //when & then
            assertThrows(NotFoundException.class,
                    () -> accountService.deleteAccount(userId, accountId));

            verify(accountDao, never()).delete(any());
        }
    }

}
