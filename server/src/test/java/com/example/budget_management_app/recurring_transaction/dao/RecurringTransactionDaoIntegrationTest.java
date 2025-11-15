package com.example.budget_management_app.recurring_transaction.dao;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.recurring_transaction.domain.RecurringInterval;
import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import com.example.budget_management_app.recurring_transaction.domain.UpcomingTransactionsTimeRange;
import com.example.budget_management_app.recurring_transaction.dto.UpcomingTransactionFilterParams;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.transaction_common.dto.PaginationParams;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/sql/transactions-test-data.sql")
@Import(RecurringTransactionDaoImpl.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RecurringTransactionDao Unit Tests")
public class RecurringTransactionDaoIntegrationTest {

    @Autowired
    private RecurringTransactionDao recurringTransactionDao;

    @Autowired
    private TestEntityManager em;

    @Nested
    @DisplayName("getSummaryTuplesByUserId() method tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetSummaryTuplesByUserIdTests {

        @Test
        @Order(1)
        @DisplayName("should return recurring transaction summaries")
        public void shouldReturnRecurringTransactionSummaryTuples() {

            // given
            Long userId = 2L;
            int expectedValue = 2;

            PaginationParams paginationParams =
                    new PaginationParams();
            paginationParams.setLimit(3);

            // when
            List<Tuple> results = recurringTransactionDao.getSummaryTuplesByUserId(
                    paginationParams,
                    userId);

            // then
            assertThat(results).isNotNull();
            assertThat(results.size()).isEqualTo(expectedValue);
            assertThat(results)
                    .extracting(tuple -> tuple.get("recId", Long.class))
                    .containsExactly( 4L, 3L);
        }
    }

    @Nested
    @DisplayName("getSummaryTuplesCountByUserId() method tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetSummaryTuplesCountByUserIdTests {

        @Test
        @Order(1)
        @DisplayName("should return correct tuples count for user transactions")
        public void shouldReturnCorrectTuplesCountForUserTransactions() {

            // given
            Long userId = 3L;
            int expectedValue = 2;

            // when
            Long count = recurringTransactionDao.getSummaryTuplesCountByUserId(userId);


            // then
            assertThat(count).isNotNull();
            assertThat(count).isEqualTo(expectedValue);
        }
    }

    @Nested
    @DisplayName("save() method tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SaveTests {

        @Test
        @Order(1)
        @DisplayName("should save recurring transaction")
        public void shouldSaveRecurringTransaction() {

            // given
            Long accountId = 1L;
            Account account = em.find(Account.class, accountId);

            Long categoryId = 4L;
            Category category = em.find(Category.class, categoryId);

            LocalDate startDate = LocalDate.now().plusDays(2);
            RecurringTransaction newRecTransaction = new RecurringTransaction(
                    new BigDecimal("200"), "HBO", TransactionType.EXPENSE, "Opłaty na full pakiet HBO",
                    startDate, null, RecurringInterval.MONTH, 1, startDate.plusMonths(1), true, LocalDateTime.now()
            );

            newRecTransaction.setCategory(category);
            newRecTransaction.setAccount(account);

            // when
            RecurringTransaction createdRecTransaction = recurringTransactionDao.save(newRecTransaction);
            em.clear();


            // then
            assertThat(createdRecTransaction).isNotNull();
            assertThat(createdRecTransaction.getId()).isGreaterThan(0L);

            RecurringTransaction fetchedRecTransaction = em.find(RecurringTransaction.class, createdRecTransaction.getId());
            assertThat(fetchedRecTransaction).isNotNull();

            Long fetchedTransactionCategoryId = fetchedRecTransaction.getCategory().getId();
            assertThat(fetchedTransactionCategoryId).isEqualTo(categoryId);

            Long fetchedTransactionAccountId = fetchedRecTransaction.getAccount().getId();
            assertThat(fetchedTransactionAccountId).isEqualTo(accountId);
        }

        @Test
        @Order(2)
        @DisplayName("should save recurring transaction with related transaction")
        public void shouldSaveRecurringTransactionWithRelatedTransactions() {

            // given
            Long accountId = 1L;
            Account account = em.find(Account.class, accountId);

            Long categoryId = 4L;
            Category category = em.find(Category.class, categoryId);

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

            // when
            RecurringTransaction createdRecTransaction = recurringTransactionDao.save(newRecTransaction);
            em.clear();

            // then
            assertThat(createdRecTransaction).isNotNull();
            assertThat(createdRecTransaction.getId()).isGreaterThan(0L);

            RecurringTransaction fetchedRecTransaction = em.find(RecurringTransaction.class, createdRecTransaction.getId());
            assertThat(fetchedRecTransaction).isNotNull();

            Optional<Transaction> optFetchedTransaction = fetchedRecTransaction.getTransactions().stream().findFirst();
            assertThat(optFetchedTransaction).isPresent();
            Transaction fetchedTransaction = optFetchedTransaction.get();

            Long fetchedRecTransactionCategoryId = fetchedRecTransaction.getCategory().getId();
            assertThat(fetchedRecTransactionCategoryId).isEqualTo(categoryId);

            Long fetchedRecTransactionAccountId = fetchedRecTransaction.getAccount().getId();
            assertThat(fetchedRecTransactionAccountId).isEqualTo(accountId);

            Long fetchedTransactionCategoryId = fetchedTransaction.getCategory().getId();
            assertThat(fetchedTransactionCategoryId).isEqualTo(categoryId);

            Long fetchedTransactionAccountId = fetchedRecTransaction.getAccount().getId();
            assertThat(fetchedTransactionAccountId).isEqualTo(accountId);
        }

    }

    @Nested
    @DisplayName("searchForRecurringTransactionsToCreate() method tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SearchForRecurringTransactionsToCreateTests {

        @Test
        @Order(1)
        @DisplayName("should find recurring transaction to create")
        public void searchForRecurringTransactionsToCreateTest() {

            int expectedCount = 2;

            // when
            List<RecurringTransaction> results = recurringTransactionDao.searchForRecurringTransactionsToCreate();

            // then
            assertThat(results).isNotNull();
            assertThat(results.size()).isEqualTo(expectedCount);
        }
    }


    @Nested
    @DisplayName("getUpcomingTransactionsTuples() method tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetUpcomingTransactionTuplesTests {

        @Test
        @Order(1)
        @DisplayName("should return upcoming transactions in range of next 7 days")
        public void shouldReturnUpcomingTransactionsInRangeOfNextSevenDays() {

            // given
            Long userId = 1L;
            LocalDate to = LocalDate.now().plusDays(7);
            LocalDate since = LocalDate.now();

            // when
            List<Tuple> results = recurringTransactionDao.getUpcomingTransactionsTuples(
                    new PaginationParams(),
                    new UpcomingTransactionFilterParams(),
                    userId);

            // then
            assertThat(results).isNotNull();
            assertThat(results.size()).isNotZero();
            assertThat(results)
                    .allSatisfy(tuple -> {
                        LocalDate transactionDate = tuple.get("nextOccurrence", LocalDate.class);
                        assertThat(transactionDate).isBetween(since, to);
                    });
        }

        @Test
        @Order(2)
        @DisplayName("should return upcoming transactions in range of next 14 days")
        public void shouldReturnUpcomingTransactionsInRangeOfNextFourteenDays() {

            // given
            Long userId = 1L;
            LocalDate to = LocalDate.now().plusDays(14);
            LocalDate since = LocalDate.now();

            UpcomingTransactionFilterParams filterParams =
                    new UpcomingTransactionFilterParams();
            filterParams.setRange(UpcomingTransactionsTimeRange.NEXT_14_DAYS);

            // when
            List<Tuple> results = recurringTransactionDao.getUpcomingTransactionsTuples(
                    new PaginationParams(),
                    new UpcomingTransactionFilterParams(),
                    userId);

            // then
            assertThat(results).isNotNull();
            assertThat(results.size()).isNotZero();
            assertThat(results)
                    .allSatisfy(tuple -> {
                        LocalDate transactionDate = tuple.get("nextOccurrence", LocalDate.class);
                        assertThat(transactionDate).isBetween(since, to);
                    });
        }

        @Test
        @Order(3)
        @DisplayName("should return upcoming transactions in range of next month")
        public void shouldReturnUpcomingTransactionsInRangeOfNextMonth() {

            // given
            Long userId = 1L;
            LocalDate to = LocalDate.now().plusMonths(1);
            LocalDate since = LocalDate.now();

            UpcomingTransactionFilterParams filterParams =
                    new UpcomingTransactionFilterParams();
            filterParams.setRange(UpcomingTransactionsTimeRange.NEXT_MONTH);

            // when
            List<Tuple> results = recurringTransactionDao.getUpcomingTransactionsTuples(
                    new PaginationParams(),
                    new UpcomingTransactionFilterParams(),
                    userId);

            // then
            assertThat(results).isNotNull();
            assertThat(results.size()).isNotZero();
            assertThat(results)
                    .allSatisfy(tuple -> {
                        LocalDate transactionDate = tuple.get("nextOccurrence", LocalDate.class);
                        assertThat(transactionDate).isBetween(since, to);
                    });
        }
    }

    @Nested
    @DisplayName("getUpcomingTransactionsCount() method tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetUpcomingTransactionsCountTest {

        @Test
        @Order(1)
        @DisplayName("should return upcoming transactions count")
        public void shouldReturnUpcomingTransactionsCount() {

            Long userId = 1L;
            Long expectedCount = 7L;

            UpcomingTransactionFilterParams filterParams =
                    new UpcomingTransactionFilterParams();
            filterParams.setRange(UpcomingTransactionsTimeRange.NEXT_MONTH);

            Long count = recurringTransactionDao.getUpcomingTransactionsCount(
                    filterParams,
                    userId);

            assertThat(count).isEqualTo(expectedCount);

            assertThat(count).isNotNull();
            assertThat(count).isGreaterThan(0L);
        }
    }
}
