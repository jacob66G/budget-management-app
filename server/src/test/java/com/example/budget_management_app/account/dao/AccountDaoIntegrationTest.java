package com.example.budget_management_app.account.dao;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.account.domain.AccountSortableField;
import com.example.budget_management_app.account.domain.AccountStatus;
import com.example.budget_management_app.account.domain.BudgetType;
import com.example.budget_management_app.account.dto.SearchCriteria;
import com.example.budget_management_app.common.enums.SupportedCurrency;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/sql/accounts-test-data.sql")
@ActiveProfiles("test")
@Import(AccountDao.class)
class AccountDaoIntegrationTest {

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private EntityManager em;

    private static final Long USER_1_ID = 1L;
    private static final Long USER_2_ID = 2L;
    private static final Long USER_5_ID = 5L;

    private final SearchCriteria EMPTY_CRITERIA = new SearchCriteria(
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null
    );

    @Test
    @DisplayName("Should find all 4 accounts for User 1 when criteria are empty")
    void should_find_all_accounts_for_user_when_criteria_is_empty() {
        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_1_ID, EMPTY_CRITERIA);

        // then
        assertThat(results).hasSize(4);
    }

    @Test
    @DisplayName("Should find accounts for User 2 and not accounts of other users")
    void should_not_find_accounts_for_other_users() {
        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_2_ID, EMPTY_CRITERIA);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Account::getId).containsExactlyInAnyOrder(4L, 5L);
    }

    @Test
    @DisplayName("Should filter by name (case-insensitive)")
    void should_filter_by_name() {
        // given
        SearchCriteria criteria = new SearchCriteria(
                null, "wallet", null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null
        );

        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_1_ID, criteria);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should filter by multiple currencies")
    void should_filter_by_multiple_currencies() {
        // given
        SearchCriteria criteria = new SearchCriteria(
                null, null, null, List.of(SupportedCurrency.USD, SupportedCurrency.EUR), null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null
        );

        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_1_ID, criteria);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should filter by status INACTIVE")
    void should_filter_by_status_inactive() {
        // given
        SearchCriteria criteria = new SearchCriteria(
                null, null, List.of(AccountStatus.INACTIVE), null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null
        );

        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_5_ID, criteria);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("Should filter by balance range (min and max)")
    void should_filter_by_balance_range() {
        // given
        SearchCriteria criteria = new SearchCriteria(
                null, null, null, null, null,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(3000), // Saldo między 1000 a 3000
                null, null, null, null, null, null, null,
                null, null, null, null
        );

        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_1_ID, criteria);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Account::getId).containsExactlyInAnyOrder(1L, 8L);
    }

    @Test
    @DisplayName("Should filter by budget type 'NONE'")
    void should_filter_by_budget_type() {
        // given
        SearchCriteria criteria = new SearchCriteria(
                null, null, null, null, List.of(BudgetType.NONE), null, null,
                null, null, null, null, null, null, null,
                null, null, null, null
        );

        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_1_ID, criteria);

        // then
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Account::getId).containsExactlyInAnyOrder(2L, 3L, 8L);
    }

    @Test
    @DisplayName("Should filter by 'includedInTotalBalance' = false")
    void should_filter_by_include_in_total_balance() {
        // given
        SearchCriteria criteria = new SearchCriteria(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, false, // includeInTotalBalance = false
                null, null, null, null
        );

        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_1_ID, criteria);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(8L);
    }

    @Test
    @DisplayName("Should filter by date 'createdAfter'")
    void should_filter_by_createdAfter() {
        // given
        LocalDate afterDate = LocalDate.of(2025, 2, 10);
        SearchCriteria criteria = new SearchCriteria(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                afterDate, null, null, null
        );

        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_1_ID, criteria);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Account::getId).containsExactlyInAnyOrder(1L, 8L);
    }

    @Test
    @DisplayName("Should filter by date 'createdBefore'")
    void should_filter_by_createdBefore() {
        // given
        LocalDate beforeDate = LocalDate.of(2025, 2, 1);
        SearchCriteria criteria = new SearchCriteria(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, beforeDate, null, null
        );

        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_1_ID, criteria);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(3L);
        assertThat(results.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should sort by createdAt (default DESC)")
    void should_sort_by_createdAt_desc_by_default() {
        // given
        SearchCriteria criteria = EMPTY_CRITERIA;

        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_1_ID, criteria);

        // then
        assertThat(results).hasSize(4);
        assertThat(results).extracting(Account::getId).containsExactly(8L, 1L, 3L, 2L);
    }

    @Test
    @DisplayName("Should sort by balance (ASC)")
    void should_sort_by_balance_asc() {
        // given
        SearchCriteria criteria = new SearchCriteria(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, AccountSortableField.BALANCE, "ASC"
        );

        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_1_ID, criteria);

        // then
        assertThat(results).hasSize(4);
        assertThat(results).extracting(Account::getId).containsExactly(3L, 1L, 8L, 2L);
    }

    @Test
    @DisplayName("Should default to createdAt sorting on empty sort field")
    void should_default_to_createdAt_on_empty_sort_field() {
        // given
        SearchCriteria criteria = new SearchCriteria(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, "ASC"
        );

        // when
        List<Account> results = accountDao.findByUserAndCriteria(USER_1_ID, criteria);

        // then
        assertThat(results).hasSize(4);
        assertThat(results).extracting(Account::getId).containsExactly(2L, 3L, 1L, 8L);
    }

    @Test
    @DisplayName("Should activate all inactive accounts for a user")
    void should_activate_all_accounts_for_user() {
        // given
        Account accountBefore = em.find(Account.class, 7L);
        assertThat(accountBefore.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(accountBefore.isIncludeInTotalBalance()).isFalse();

        // when
        accountDao.activateAll(USER_5_ID);

        // then
        em.clear();

        Account accountAfter = em.find(Account.class, 7L);
        assertThat(accountAfter).isNotNull();
        assertThat(accountAfter.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(accountAfter.isIncludeInTotalBalance()).isTrue();
    }

    @Test
    @DisplayName("Should deactivate all active accounts for a user")
    void should_deactivate_all_accounts_for_user() {
        // given
        // User 2 has two accounts (ID 4 and 5), both are ACTIVE.
        Account account4Before = em.find(Account.class, 4L);
        Account account5Before = em.find(Account.class, 5L);
        assertThat(account4Before.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account5Before.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        // check account other user (User 1)
        Account user1Account = em.find(Account.class, 1L);
        assertThat(user1Account.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);

        // when
        accountDao.deactivateAll(USER_2_ID);

        // then
        em.clear();

        // check if user 2 accounts have been updated
        Account account4After = em.find(Account.class, 4L);
        Account account5After = em.find(Account.class, 5L);
        assertThat(account4After.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(account4After.isIncludeInTotalBalance()).isFalse();
        assertThat(account5After.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(account5After.isIncludeInTotalBalance()).isFalse();

        // check if user 1 account has not been updated
        Account user1AccountAfter = em.find(Account.class, 1L);
        assertThat(user1AccountAfter.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should delete all accounts for a user")
    void should_delete_all_accounts_for_user() {
        // given
        // User 2 has two accounts (ID 4 i 5). User 1 has four accounts (ID 1).
        assertThat(em.find(Account.class, 4L)).isNotNull();
        assertThat(em.find(Account.class, 5L)).isNotNull();
        assertThat(em.find(Account.class, 1L)).isNotNull(); // User 1 account

        // when
        accountDao.deleteAll(USER_2_ID);

        // then
        em.clear();

        // check if user 2 accounts have been deleted
        assertThat(em.find(Account.class, 4L)).isNull();
        assertThat(em.find(Account.class, 5L)).isNull();

        // check that user 1 account has not been deleted
        assertThat(em.find(Account.class, 1L)).isNotNull();
    }
}