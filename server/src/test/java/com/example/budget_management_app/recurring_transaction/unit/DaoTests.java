package com.example.budget_management_app.recurring_transaction.unit;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.recurring_transaction.dao.RecurringTransactionDao;
import com.example.budget_management_app.recurring_transaction.domain.RecurringInterval;
import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction.domain.TransactionType;
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
@DisplayName("RecurringTransactionDao Unit Tests")
public class DaoTests {

    @Autowired
    private RecurringTransactionDao recurringTransactionDao;

    @Autowired
    private EntityManager em;

    @Test
    @Order(1)
    public void getRecurringTransactionsSummaryTuplesByUserIdTest() {

        long userId = 1L;
        int page = 1;
        int limit = 3;
        long expectedValue = 2L;

        List<Tuple> results = recurringTransactionDao.getSummaryTuplesByUserId(userId, page, limit);

        assertThat(results).isNotNull();
        assertThat(results.size()).isNotZero();
        assertThat(results.size()).isEqualTo(expectedValue);

        results.forEach(System.out::println);
    }

    @Test
    @Order(2)
    public void getRecurringTransactionsSummaryCountByUserIdTest() {

        long userId = 1L;
        long expectedValue = 2L;

        Long count = recurringTransactionDao.getSummaryTuplesCountByUserId(userId);

        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(expectedValue);
    }

    @Test
    @Order(3)
    @Transactional(readOnly = true)
    public void findRecurringTransactionTemplateByIdAndUserIdTest() {

        long userId = 1L;
        long recurringTemplateId = 2L;

        Optional<RecurringTransaction> optRecurringTransaction = recurringTransactionDao.findByIdAndUserId(recurringTemplateId, userId);
        assertThat(optRecurringTransaction).isPresent();

        RecurringTransaction recTransaction = optRecurringTransaction.get();
        assertThat(recTransaction.getId()).isEqualTo(recurringTemplateId);
        assertThat(recTransaction.getAccount().getUser().getId()).isEqualTo(userId);
        assertThat(recTransaction.getTitle()).isEqualTo("Czynsz");
    }

    @Test
    @Order(4)
    @Transactional
    public void createRecurringTransactionWithoutRegularTransactionTest() {

        long accountId = 1L;
        Account account = em.find(Account.class, accountId);
        assertThat(account).isNotNull();

        long categoryId = 4L;
        Category category = em.find(Category.class, categoryId);
        assertThat(category).isNotNull();

        LocalDate startDate = LocalDate.now().plusDays(2);
        RecurringTransaction newRecTransaction = new RecurringTransaction(
                new BigDecimal("200"), "HBO", TransactionType.EXPENSE, "Opłaty na full pakiet HBO",
                startDate, null, RecurringInterval.MONTH, 1, startDate.plusMonths(1), true, LocalDateTime.now()
        );

        newRecTransaction.setCategory(category);
        newRecTransaction.setAccount(account);

        RecurringTransaction createdRecTransaction = recurringTransactionDao.create(newRecTransaction);
        em.clear();

        RecurringTransaction fetchedRecTransaction = em.find(RecurringTransaction.class, createdRecTransaction.getId());

        assertThat(fetchedRecTransaction).isNotNull();
        assertThat(fetchedRecTransaction.getId()).isEqualTo(createdRecTransaction.getId());

        Long fetchedTransactionCategoryId = fetchedRecTransaction.getCategory().getId();
        assertThat(fetchedTransactionCategoryId).isEqualTo(categoryId);

        Long fetchedTransactionAccountId = fetchedRecTransaction.getAccount().getId();
        assertThat(fetchedTransactionAccountId).isEqualTo(accountId);

        System.out.println(fetchedRecTransaction + " categoryId: " + fetchedTransactionCategoryId +
                " accountId: " + fetchedTransactionAccountId);
    }

    @Test
    @Order(5)
    @Transactional
    public void createRecurringTransactionWithRegularTransaction() {

        long accountId = 1L;
        Account account = em.find(Account.class, accountId);
        assertThat(account).isNotNull();

        long categoryId = 4L;
        Category category = em.find(Category.class, categoryId);
        assertThat(category).isNotNull();

        BigDecimal amount = new BigDecimal("200");
        String title = "HBO";
        String description = "Opłaty na full pakiet HBO";
        LocalDate startDate = LocalDate.now();

        RecurringTransaction newRecTransaction = new RecurringTransaction(
                amount, title, TransactionType.EXPENSE, description,
                startDate, null, RecurringInterval.MONTH, 1, startDate.plusMonths(1), true, LocalDateTime.now()
        );

        Transaction newTransaction = new Transaction(
                amount, title, TransactionType.EXPENSE, description, LocalDateTime.now()
        );

        newTransaction.setCategory(category);
        newTransaction.setAccount(account);

        newRecTransaction.addTransaction(newTransaction);
        newRecTransaction.setCategory(category);
        newRecTransaction.setAccount(account);

        RecurringTransaction createdRecTransaction = recurringTransactionDao.create(newRecTransaction);
        em.clear();

        RecurringTransaction fetchedRecTransaction = em.find(RecurringTransaction.class, createdRecTransaction.getId());
        assertThat(fetchedRecTransaction).isNotNull();

        Optional<Transaction> optFetchedTransaction = fetchedRecTransaction.getTransactions().stream().findFirst();
        assertThat(optFetchedTransaction).isPresent();
        Transaction fetchedTransaction = optFetchedTransaction.get();

        long expectedTransactionIdValue = fetchedTransaction.getId();
        assertThat(fetchedRecTransaction.getId()).isEqualTo(createdRecTransaction.getId());
        assertThat(fetchedTransaction.getId()).isEqualTo(expectedTransactionIdValue);

        Long fetchedRecTransactionCategoryId = fetchedRecTransaction.getCategory().getId();
        assertThat(fetchedRecTransactionCategoryId).isEqualTo(categoryId);

        Long fetchedRecTransactionAccountId = fetchedRecTransaction.getAccount().getId();
        assertThat(fetchedRecTransactionAccountId).isEqualTo(accountId);

        Long fetchedTransactionCategoryId = fetchedTransaction.getCategory().getId();
        assertThat(fetchedTransactionCategoryId).isEqualTo(categoryId);

        Long fetchedTransactionAccountId = fetchedRecTransaction.getAccount().getId();
        assertThat(fetchedTransactionAccountId).isEqualTo(accountId);


        System.out.println(fetchedRecTransaction + " categoryId: " + fetchedRecTransactionCategoryId +
                " accountId: " + fetchedRecTransactionAccountId);
        System.out.println("\n" + fetchedTransaction + " categoryId: " + fetchedTransactionCategoryId +
                " accountId: " + fetchedTransactionAccountId);
    }

    @Test
    @Order(6)
    public void searchForRecurringTransactionsToCreateTest() {

        long expectedCount = 1L;

        List<RecurringTransaction> results = recurringTransactionDao.searchForRecurringTransactionsToCreate();

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(expectedCount);

        results.forEach(System.out::println);
    }

}
