package com.example.budget_management_app.transaction.unit;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.transaction.dao.TransactionDao;
import com.example.budget_management_app.transaction.domain.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TransactionDao Unit Tests")
public class DaoTests {

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private EntityManager em;

    @Test
    @Order(1)
    public void getTransactionsCountWithDefaultFiltersTest() {

        long expectedNumberOfRecords = 10L;
        List<Long> allUserAccountsIds = List.of(1L, 2L, 3L);
        List<Long> allUserCategoriesIds = List.of(1L, 2L, 3L, 4L);
        Long count = transactionDao.getCount(
                TransactionTypeFilter.ALL,
                TransactionModeFilter.ALL,
                allUserAccountsIds,
                allUserCategoriesIds,
                LocalDate.of(2025,10,1),
                null
                );

        assertThat(count).isNotNull();
        assertThat(count).isNotZero();
        assertThat(count).isEqualTo(expectedNumberOfRecords);
    }

    @Test
    @Order(2)
    public void getTransactionsCountForRecurringTransactionsTest() {

        long expectedNumberOfRecords = 4L;
        List<Long> allUserAccountsIds = List.of(1L, 2L, 3L);
        List<Long> allUserCategoriesIds = List.of(1L, 2L, 3L, 4L);
        Long count = transactionDao.getCount(
                TransactionTypeFilter.ALL,
                TransactionModeFilter.RECURRING,
                allUserAccountsIds,
                allUserCategoriesIds,
                LocalDate.of(2025, 9, 1),
                null
        );

        assertThat(count).isNotNull();
        assertThat(count).isNotZero();
        assertThat(count).isEqualTo(expectedNumberOfRecords);
    }

    @Test
    @Order(3)
    public void getTransactionsCountForRegularTransactionsTest() {

        long expectedNumberOfRecords = 8L;
        List<Long> allUserAccountsIds = List.of(1L, 2L, 3L);
        List<Long> allUserCategoriesIds = List.of(1L, 2L, 3L, 4L);
        Long count = transactionDao.getCount(
                TransactionTypeFilter.ALL,
                TransactionModeFilter.REGULAR,
                allUserAccountsIds,
                allUserCategoriesIds,
                LocalDate.of(2025, 9, 1),
                null
        );

        assertThat(count).isNotNull();
        assertThat(count).isNotZero();
        assertThat(count).isEqualTo(expectedNumberOfRecords);
    }

    @Test
    @Order(4)
    public void getRegularTransactionsCountForParticularAccountAndCategoriesTest() {

        long expectedNumberOfRecords = 3L;
        List<Long> allUserAccountsIds = List.of(1L);
        List<Long> allUserCategoriesIds = List.of(2L, 4L);
        Long count = transactionDao.getCount(
                TransactionTypeFilter.ALL,
                TransactionModeFilter.REGULAR,
                allUserAccountsIds,
                allUserCategoriesIds,
                LocalDate.of(2025, 9, 1),
                null
        );

        assertThat(count).isNotNull();
        assertThat(count).isNotZero();
        assertThat(count).isEqualTo(expectedNumberOfRecords);
    }

    @Test
    @Order(5)
    public void getRecurringTransactionsCountForParticularAccountAndCategoryTest() {

        long expectedNumberOfRecords = 4L;
        List<Long> allUserAccountsIds = List.of(1L);
        List<Long> allUserCategoriesIds = List.of(4L);
        Long count = transactionDao.getCount(
                TransactionTypeFilter.ALL,
                TransactionModeFilter.RECURRING,
                allUserAccountsIds,
                allUserCategoriesIds,
                LocalDate.of(2025, 9, 1),
                null
        );

        assertThat(count).isNotNull();
        assertThat(count).isNotZero();
        assertThat(count).isEqualTo(expectedNumberOfRecords);
    }

    @Test
    @Order(6)
    public void getTransactionTuplesFirstPageWithDefaultFiltersTest() {

        int page = 1;
        int limit = 5;
        int expectedSize = 5;
        List<Long> allUserAccountsIds = List.of(1L, 2L, 3L);
        List<Long> allUserCategoriesIds = List.of(1L, 2L, 3L, 4L);
        List<Tuple> transactions = transactionDao.getTuples(
                page,
                limit,
                TransactionTypeFilter.ALL,
                TransactionModeFilter.ALL,
                allUserAccountsIds,
                allUserCategoriesIds,
                LocalDate.of(2025, 9, 1),
                null,
                SortedBy.DATE,
                SortDirection.DESC);

        assertThat(transactions).isNotNull();
        assertThat(transactions.size()).isNotZero();
        assertThat(transactions.size()).isEqualTo(expectedSize);

        transactions.forEach(System.out::println);
    }

    @Test
    @Order(7)
    public void getTransactionTuplesLastPageForAllUserTransactionsTest() {

        int page = 3;
        int limit = 5;
        int expectedSize = 2;
        List<Long> allUserAccountsIds = List.of(1L, 2L, 3L);
        List<Long> allUserCategoriesIds = List.of(1L, 2L, 3L, 4L);
        List<Tuple> transactions = transactionDao.getTuples(
                page,
                limit,
                TransactionTypeFilter.ALL,
                TransactionModeFilter.ALL,
                allUserAccountsIds,
                allUserCategoriesIds,
                LocalDate.of(2025, 9, 1),
                null,
                SortedBy.DATE,
                SortDirection.DESC);

        assertThat(transactions).isNotNull();
        assertThat(transactions.size()).isNotZero();
        assertThat(transactions.size()).isEqualTo(expectedSize);

        transactions.forEach(System.out::println);
    }

    @Test
    @Order(8)
    public void getAllExpenseTypeTransactionTuplesFirstPageForCustomDataAndTest() {

        int page = 1;
        int limit = 5;
        int expectedSize = 5;
        List<Long> allUserAccountsIds = List.of(1L, 2L, 3L);
        List<Long> allUserCategoriesIds = List.of(1L, 2L, 3L, 4L);
        List<Tuple> transactions = transactionDao.getTuples(
                page,
                limit,
                TransactionTypeFilter.EXPENSE,
                TransactionModeFilter.ALL,
                allUserAccountsIds,
                allUserCategoriesIds,
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025,10, 21),
                SortedBy.DATE,
                SortDirection.DESC);

        assertThat(transactions).isNotNull();
        assertThat(transactions.size()).isNotZero();
        assertThat(transactions.size()).isEqualTo(expectedSize);

        transactions.forEach(System.out::println);
    }

    @Test
    @Order(9)
    public void getAllExpenseTypeTransactionTuplesLastPageForCustomDataAndTest() {

        int page = 2;
        int limit = 5;
        int expectedSize = 2;
        List<Long> allUserAccountsIds = List.of(1L, 2L, 3L);
        List<Long> allUserCategoriesIds = List.of(1L, 2L, 3L, 4L);
        List<Tuple> transactions = transactionDao.getTuples(
                page,
                limit,
                TransactionTypeFilter.EXPENSE,
                TransactionModeFilter.ALL,
                allUserAccountsIds,
                allUserCategoriesIds,
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025,10, 21),
                SortedBy.DATE,
                SortDirection.DESC);

        assertThat(transactions).isNotNull();
        assertThat(transactions.size()).isNotZero();
        assertThat(transactions.size()).isEqualTo(expectedSize);

        transactions.forEach(System.out::println);
    }

    @Test
    @Order(10)
    @Transactional
    public void saveRegularTransactionTest() {

        long expectedIdValue = 61L;
        long categoryId = 1L;
        long accountId = 1L;

        Transaction transaction = new Transaction(
                new BigDecimal("200"), "Książki", TransactionType.EXPENSE,
                "Książki do szkoły", LocalDateTime.now()
        );

        Category category = em.find(Category.class, categoryId);
        assertThat(category).isNotNull();

        Account account = em.find(Account.class, accountId);
        assertThat(account).isNotNull();

        transaction.setCategory(category);
        transaction.setAccount(account);

        Transaction savedTransaction = transactionDao.save(transaction);
        assertThat(savedTransaction).isNotNull();
        assertThat(savedTransaction.getId()).isNotNull();
        assertThat(savedTransaction.getId()).isEqualTo(expectedIdValue);

        em.clear();

        Transaction newFetchedTransaction = em.find(Transaction.class, savedTransaction.getId());
        assertThat(newFetchedTransaction).isNotNull();
        assertThat(newFetchedTransaction.getCategory()).isNotNull();
        assertThat(newFetchedTransaction.getCategory().getId()).isEqualTo(categoryId);
        assertThat(newFetchedTransaction.getAccount()).isNotNull();
        assertThat(newFetchedTransaction.getAccount().getId()).isEqualTo(accountId);

        System.out.println(newFetchedTransaction);
    }

    @Test
    @Order(11)
    @Transactional
    public void deleteRegularTransactionTest() {

        long transactionId = 28L;
        Transaction transactionToDelete = em.find(Transaction.class, transactionId);
        transactionToDelete.removeCategory();
        transactionToDelete.removeAccount();
        transactionDao.delete(transactionToDelete);

        em.flush();
        em.clear();

        Transaction transaction = em.find(Transaction.class, transactionId);
        assertThat(transaction).isNull();

    }

    @Test
    @Order(12)
    @Transactional(readOnly = true)
    public void findTransactionsByIdAndUserId() {

        long userId = 1;
        long transactionId = 26L;
        long expectedAccountId = 1L;
        long expectedCategoryId = 4L;

        Optional<Transaction> optTransaction = transactionDao.findByIdAndUserId(transactionId, userId);
        assertThat(optTransaction).isPresent();

        Transaction transaction = optTransaction.get();
        assertThat(transaction.getId()).isEqualTo(transactionId);
        assertThat(transaction.getAccount().getId()).isEqualTo(expectedAccountId);
        assertThat(transaction.getCategory().getId()).isEqualTo(expectedCategoryId);
        assertThat(transaction.getAccount().getUser().getId()).isEqualTo(userId);

    }

    @Test
    @Order(13)
    @Transactional(readOnly = true)
    public void findRecurringTransactionByRecurringTemplateId() {

        long recurringTemplateId = 1L;
        long expectedValue = 2L;

        List<Transaction> result = transactionDao.findByRecurringTransactionId(recurringTemplateId);
        assertThat(result).isNotNull();
        assertThat(result.size()).isNotZero();
        assertThat(result.size()).isEqualTo(expectedValue);

        result.forEach(System.out::println);
    }
}
