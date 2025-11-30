package com.example.budget_management_app.account.service;

import com.example.budget_management_app.account.dao.AccountDao;
import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.account.domain.AccountStatus;
import com.example.budget_management_app.account.domain.AccountType;
import com.example.budget_management_app.account.domain.BudgetType;
import com.example.budget_management_app.account.dto.AccountCreateRequest;
import com.example.budget_management_app.account.dto.AccountDetailsResponse;
import com.example.budget_management_app.account.dto.AccountUpdateRequest;
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
    private UserDao userDao;
    @Mock
    private StorageService storageService;
    @Mock
    private IconKeyValidator iconKeyValidator;
    @Mock
    private RecurringTransactionService recurringTransactionService;
    @Mock
    private TransactionService transactionService;
    @InjectMocks
    private AccountServiceImpl accountService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Nested
    class CreateAccountTests {

        private User user;
        private AccountCreateRequest requestDto;
        private Account savedAccount;
        private AccountDetailsResponse expectedResult;
        private Long userId = 1L;
        private String accountName = "main";
        private String iconKey = "/accounts/test.png";
        private BigDecimal balance = BigDecimal.valueOf(1000);
        private String description = "my main account";
        private BigDecimal budget = BigDecimal.valueOf(500);
        private Double alertThreshold = 20.0;

        @BeforeEach
        void setUp() {
            user = new User();
            user.setId(userId);

            requestDto = new AccountCreateRequest(
                    "PERSONAL",
                    accountName,
                    "PLN",
                    description,
                    balance,
                    "MONTHLY",
                    budget,
                    alertThreshold,
                    true,
                    iconKey
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
            savedAccount.setIconKey(iconKey);
            savedAccount.setIncludeInTotalBalance(true);
            savedAccount.setUser(user);

            expectedResult = new AccountDetailsResponse(
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
                    iconKey,
                    true,
                    savedAccount.getCreatedAt(),
                    "ACTIVE",
                    false
            );

            lenient().when(transactionService.existsByAccountAndUser(any(), any())).thenReturn(false);
            lenient().when(storageService.extractKey(anyString())).thenReturn(iconKey);
        }

        @Test
        void should_create_new_account() {
            //given
            when(userDao.findById(userId)).thenReturn(Optional.of(user));
            when(accountDao.existsByNameAndUser(accountName, userId, null)).thenReturn(false);
            when(iconKeyValidator.isValidAccountIconKey(iconKey)).thenReturn(true);
            when(accountDao.save(any(Account.class))).thenReturn(savedAccount);
            when(mapper.toDetailsResponse(savedAccount, false)).thenReturn(expectedResult);

            //When
            AccountDetailsResponse result = accountService.createAccount(userId, requestDto);

            //then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedResult);
            assertThat(result.name()).isEqualTo(accountName);

            verify(userDao, times(1)).findById(userId);
            verify(accountDao, times(1)).existsByNameAndUser(accountName, userId, null);
            verify(iconKeyValidator, times(1)).isValidAccountIconKey(iconKey);
            verify(mapper, times(1)).toDetailsResponse(savedAccount, false);

            verify(accountDao, times(1)).save(accountCaptor.capture());
            Account accountSentToDao = accountCaptor.getValue();

            assertThat(accountSentToDao).isNotNull();
            assertThat(accountSentToDao.getName()).isEqualTo(requestDto.name());
            assertThat(accountSentToDao.getBalance()).isEqualTo(requestDto.initialBalance());
            assertThat(accountSentToDao.getCurrency().name()).isEqualTo(requestDto.currency());
            assertThat(accountSentToDao.getBudgetType().name()).isEqualTo(requestDto.budgetType());
            assertThat(accountSentToDao.getIconKey()).isEqualTo(requestDto.iconPath());
            assertThat(accountSentToDao.getUser()).isEqualTo(user);

            assertThat(accountSentToDao.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(accountSentToDao.isDefault()).isFalse();
            assertThat(accountSentToDao.getCreatedAt()).isNotNull();
        }

        @Test
        void should_throw_validationException_when_incorrect_account_type() {
            //given
            String accountWrongType = "no existent type";
            AccountCreateRequest requestWithWrongType = new AccountCreateRequest(
                    accountWrongType,
                    accountName,
                    "PLN",
                    description,
                    balance,
                    "MONTHLY",
                    budget,
                    alertThreshold,
                    true,
                    iconKey
            );
            when(userDao.findById(userId)).thenReturn(Optional.of(user));

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithWrongType));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WRONG_TYPE);
            assertThat(exception.getMessage()).contains(accountWrongType);
            verify(userDao, times(1)).findById(userId);
        }

        @Test
        void should_throw_validationException_when_incorrect_currency() {
            //given
            String wrongCurrency = "no existent currency";
            AccountCreateRequest requestWithWrongCurrency = new AccountCreateRequest(
                    "PERSONAL",
                    accountName,
                    wrongCurrency,
                    description,
                    balance,
                    "MONTHLY",
                    budget,
                    alertThreshold,
                    true,
                    iconKey
            );
            when(userDao.findById(userId)).thenReturn(Optional.of(user));

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithWrongCurrency));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WRONG_CURRENCY);
            assertThat(exception.getMessage()).contains(wrongCurrency);
            verify(userDao, times(1)).findById(userId);
        }

        @Test
        void should_throw_validationException_when_incorrect_budget_type() {
            //given
            String budgetWrongType = "no existent budget type";
            AccountCreateRequest requestWithWrongBudgetType = new AccountCreateRequest(
                    "PERSONAL",
                    accountName,
                    "PLN",
                    description,
                    balance,
                    budgetWrongType,
                    budget,
                    alertThreshold,
                    true,
                    iconKey
            );
            when(userDao.findById(userId)).thenReturn(Optional.of(user));

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithWrongBudgetType));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WRONG_BUDGET_TYPE);
            assertThat(exception.getMessage()).contains(budgetWrongType);
            verify(userDao, times(1)).findById(userId);
        }

        @Test
        void should_throw_validationException_when_user_have_account_with_the_same_name() {
            //given
            when(userDao.findById(userId)).thenReturn(Optional.of(user));
            when(accountDao.existsByNameAndUser(accountName, userId, null)).thenReturn(true);

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestDto));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NAME_ALREADY_USED);
            assertThat(exception.getMessage()).contains(accountName);

            verify(userDao, times(1)).findById(userId);
            verify(accountDao, times(1)).existsByNameAndUser(accountName, userId, null);
        }

        @Test
        void should_throw_validationException_when_budget_type_is_none_and_budget_is_not_null() {
            //given
            AccountCreateRequest requestWithBudget = new AccountCreateRequest(
                    "PERSONAL",
                    accountName,
                    "PLN",
                    description,
                    balance,
                    "NONE",
                    budget,
                    null,
                    true,
                    iconKey
            );
            when(userDao.findById(userId)).thenReturn(Optional.of(user));

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithBudget));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_BUDGET_ALERT_RELATION);

            verify(userDao, times(1)).findById(userId);
        }

        @Test
        void should_throw_validationException_when_budget_type_is_none_and_alertThreshold_is_not_null() {
            //given
            AccountCreateRequest requestWithBudget = new AccountCreateRequest(
                    "PERSONAL",
                    accountName,
                    "PLN",
                    description,
                    balance,
                    "NONE",
                    null,
                    alertThreshold,
                    true,
                    iconKey
            );
            when(userDao.findById(userId)).thenReturn(Optional.of(user));

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithBudget));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_BUDGET_ALERT_RELATION);

            verify(userDao, times(1)).findById(userId);
        }

        @Test
        void should_throw_validationException_when_budget_type_is_not_none_and_budget_is_null() {
            //given
            AccountCreateRequest requestWithBudget = new AccountCreateRequest(
                    "PERSONAL",
                    accountName,
                    "PLN",
                    description,
                    balance,
                    "WEEKLY",
                    null,
                    alertThreshold,
                    true,
                    iconKey
            );
            when(userDao.findById(userId)).thenReturn(Optional.of(user));

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithBudget));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_BUDGET_ALERT_RELATION);

            verify(userDao, times(1)).findById(userId);
        }

        @Test
        void should_throw_validationException_when_budget_type_is_not_none_and_alertThreshold_is_null() {
            //given
            AccountCreateRequest requestWithBudget = new AccountCreateRequest(
                    "PERSONAL",
                    accountName,
                    "PLN",
                    description,
                    balance,
                    "WEEKLY",
                    budget,
                    null,
                    true,
                    iconKey
            );
            when(userDao.findById(userId)).thenReturn(Optional.of(user));

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestWithBudget));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_BUDGET_ALERT_RELATION);

            verify(userDao, times(1)).findById(userId);
        }

        @Test
        void should_throw_validationException_when_icon_path_is_invalid() {
            //given
            when(userDao.findById(userId)).thenReturn(Optional.of(user));
            when(iconKeyValidator.isValidAccountIconKey(iconKey)).thenReturn(false);

            //when + then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> accountService.createAccount(userId, requestDto));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_RESOURCE_PATH);

            verify(userDao, times(1)).findById(userId);
            verify(iconKeyValidator, times(1)).isValidAccountIconKey(iconKey);
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

            lenient().when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.of(existingAccount));
            lenient().when(transactionService.existsByAccountAndUser(any(), any())).thenReturn(false);
        }

        @Test
        void should_update_simple_fields_successfully() {
            //given
            String newName = "new name";
            String newDesc = "new des";
            String newIcon = "https://new.path/icon.png";

            AccountUpdateRequest dto = new AccountUpdateRequest(
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

            when(storageService.extractKey(anyString())).thenReturn(newIcon);
            when(accountDao.existsByNameAndUser(newName, userId, accountId)).thenReturn(false);
            when(iconKeyValidator.isValidAccountIconKey(newIcon)).thenReturn(true);

            when(accountDao.update(any(Account.class))).thenReturn(existingAccount);
            when(mapper.toDetailsResponse(existingAccount, false)).thenReturn(mock(AccountDetailsResponse.class));

            //when
            accountService.updateAccount(userId, accountId, dto);

            //then
            verify(accountDao, times(1)).update(accountCaptor.capture());
            verify(mapper, times(1)).toDetailsResponse(existingAccount, false);

            Account updatedAccount = accountCaptor.getValue();
            assertThat(updatedAccount.getName()).isEqualTo(newName);
            assertThat(updatedAccount.getDescription()).isEqualTo(newDesc);
            assertThat(updatedAccount.getIconKey()).isEqualTo(newIcon);
            assertThat(updatedAccount.isIncludeInTotalBalance()).isFalse();
        }

        @Test
        void should_throw_ValidationException_when_updating_inactive_account() {
            //given
            existingAccount.setAccountStatus(AccountStatus.INACTIVE);
            AccountUpdateRequest dto = new AccountUpdateRequest(
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
            verify(mapper, never()).toDetailsResponse(any(), eq(false));
        }

        @Test
        void should_throw_ValidationException_when_new_name_is_already_used() {
            //given
            String newName = "existing name";
            AccountUpdateRequest dto = new AccountUpdateRequest(
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
            verify(mapper, never()).toDetailsResponse(any(), eq(false));
        }

        @Test
        void should_update_currency() {
            //given
            String newCurrency = "USD";
            AccountUpdateRequest dto = new AccountUpdateRequest(
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

            //when
            accountService.updateAccount(userId, accountId, dto);

            //then
            verify(accountDao, times(1)).update(accountCaptor.capture());
            Account updatedAccount = accountCaptor.getValue();
            assertThat(updatedAccount.getCurrency()).isEqualTo(SupportedCurrency.USD);
        }

        @Test
        void should_update_initialBalance() {
            //given
            BigDecimal newBalance = BigDecimal.valueOf(999.00);
            AccountUpdateRequest dto = new AccountUpdateRequest(
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
            when(mapper.toDetailsResponse(existingAccount, false)).thenReturn(mock(AccountDetailsResponse.class));
            when(transactionService.existsByAccountAndUser(accountId, userId)).thenReturn(false);

            //when
            accountService.updateAccount(userId, accountId, dto);

            //then
            verify(transactionService, times(2)).existsByAccountAndUser(accountId, userId);
            verify(accountDao, times(1)).update(accountCaptor.capture());
            Account updatedAccount = accountCaptor.getValue();
            assertThat(updatedAccount.getBalance()).isEqualByComparingTo(newBalance);
        }

        @Test
        void should_throw_ValidationException_when_change_initial_balance_when_transactions_exists() {
            //given
            BigDecimal newBalance = BigDecimal.valueOf(999.00);
            AccountUpdateRequest dto = new AccountUpdateRequest(
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

            when(transactionService.existsByAccountAndUser(accountId, userId)).thenReturn(true);

            //when + then
            ValidationException ex = assertThrows(ValidationException.class, () -> accountService.updateAccount(userId, accountId, dto));

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MODIFY_BUDGET_WITH_TRANSACTIONS);
            verify(accountDao, never()).update(any());
            verify(mapper, never()).toDetailsResponse(any(), eq(false));
        }


        // --- changeBudget tests ---

        @Test
        void changeBudget_should_clear_fields_when_type_is_set_to_NONE() {
            //given
            existingAccount.setBudgetType(BudgetType.MONTHLY);
            existingAccount.setBudget(BigDecimal.valueOf(1000));
            existingAccount.setAlertThreshold(50.0);

            AccountUpdateRequest dto = new AccountUpdateRequest(
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
            when(mapper.toDetailsResponse(existingAccount, false)).thenReturn(mock(AccountDetailsResponse.class));

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
            AccountUpdateRequest dto = new AccountUpdateRequest(
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
            AccountUpdateRequest dto = new AccountUpdateRequest(
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
            AccountUpdateRequest dto = new AccountUpdateRequest(
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
            when(mapper.toDetailsResponse(existingAccount, false)).thenReturn(mock(AccountDetailsResponse.class));

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
            AccountUpdateRequest dto = new AccountUpdateRequest(
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
            when(mapper.toDetailsResponse(existingAccount, false)).thenReturn(mock(AccountDetailsResponse.class));

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

            lenient().when(transactionService.existsByAccountAndUser(any(), any())).thenReturn(false);
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
            verify(recurringTransactionService, times(1)).activateAllByAccount(accountId, userId);
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
            verify(recurringTransactionService, never()).activateAllByAccount(anyLong(), anyLong());
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

            lenient().when(transactionService.existsByAccountAndUser(any(), any())).thenReturn(false);
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

            verify(recurringTransactionService, times(1)).deactivateAllByAccount(accountId, userId);
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
            verify(recurringTransactionService, never()).deactivateAllByAccount(anyLong(), anyLong());
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

            lenient().when(transactionService.existsByAccountAndUser(any(), any())).thenReturn(false);
        }

        @Test
        void should_delete_account_successfully() {
            //given
            // Status INACTIVE and not-default set w setUp
            when(accountDao.findByIdAndUser(userId, accountId)).thenReturn(Optional.of(account));

            //when
            accountService.deleteAccount(userId, accountId);

            //then
            verify(transactionService, times(1)).deleteAllByAccount(accountId, userId);
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
            verify(transactionService, never()).deleteAllByAccount(anyLong(), anyLong());
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
            verify(transactionService, never()).deleteAllByAccount(anyLong(), anyLong());
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
