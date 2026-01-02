package com.example.budget_management_app.transaction.dao;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.transaction.domain.*;
import com.example.budget_management_app.transaction.dto.TransactionFilterParams;
import com.example.budget_management_app.transaction.dto.TransactionPaginationParams;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/sql/transactions-test-data.sql")
@ActiveProfiles("test")
@Import(TransactionDaoImpl.class)
@DisplayName("TransactionDao Integration Tests")
public class TransactionDaoIntegrationTest {

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private EntityManager em;

    private final int DEFAULT_PAGE_LIMIT = 8;

    @Nested
    @DisplayName("getCount() method tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetCountTests {

        @Test
        @Order(1)
        @DisplayName("should return all user transactions count when default filters and pagination are applied")
        public void shouldReturnAllUserTransactionsCount_whenDefaultFiltersAndPaginationRulesAreApplied() {

            // given
            Long userId = 1L;
            Long expectedNumberOfRecords = 9L;

            // when
            Long count = transactionDao.getCount(
                    new TransactionFilterParams(),
                    userId
            );

            // then
            assertThat(count).isNotNull();
            assertThat(count).isEqualTo(expectedNumberOfRecords);
        }

        @Test
        @Order(2)
        @DisplayName("should return user expense transactions count when they exist")
        public void shouldReturnUserExpenseTransactionsCorrectCount_whenTheyExists() {

            // given
            Long userId = 2L;
            Long expectedNumberOfRecords = 7L;
            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setType(TransactionTypeFilter.EXPENSE);

            // when
            Long count = transactionDao.getCount(
                    filterParams,
                    userId
            );

            // then
            assertThat(count).isNotNull();
            assertThat(count).isEqualTo(expectedNumberOfRecords);
        }

        @Test
        @Order(3)
        @DisplayName("should return user income transactions count when they exist")
        public void shouldReturnUserIncomeTransactionsCorrectCount_whenTheyExists() {

            // given
            Long userId = 3L;
            Long expectedNumberOfRecords = 2L;
            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setType(TransactionTypeFilter.INCOME);

            // when
            Long count = transactionDao.getCount(
                    filterParams,
                    userId
            );

            // then
            assertThat(count).isNotNull();
            assertThat(count).isEqualTo(expectedNumberOfRecords);
        }

        @Test
        @Order(4)
        @DisplayName("should return user regular transactions count when they exist")
        public void shouldReturnUserRegularTransactionsCount_whenTheyExists() {

            // given
            Long userId = 4L;
            Long expectedNumberOfRecords = 7L;
            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setMode(TransactionModeFilter.REGULAR);

            // when
            Long count = transactionDao.getCount(
                    filterParams,
                    userId
            );

            // then
            assertThat(count).isNotNull();
            assertThat(count).isEqualTo(expectedNumberOfRecords);
        }

        @Test
        @Order(5)
        @DisplayName("should return user recurring transactions count when they exist")
        public void shouldReturnUserRecurringTransactionsCount_whenTheyExists() {

            // given
            Long userId = 5L;
            Long expectedNumberOfRecords = 2L;
            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setMode(TransactionModeFilter.RECURRING);

            // when
            Long count = transactionDao.getCount(
                    filterParams,
                    userId
            );

            // then
            assertThat(count).isNotNull();
            assertThat(count).isEqualTo(expectedNumberOfRecords);
        }

//        @Test
//        @Order(6)
//        @DisplayName("should return all user transactions count when custom date range is applied")
//        public void shouldReturnUserTransactionsCount_whenCustomDateRangeApplied() {
//
//            // given
//            Long userId = 2L;
//            Long expectedNumberOfRecords = 3L;
//            LocalDate since = LocalDate.now().minusMonths(1).minusDays(20);
//            LocalDate to = LocalDate.now().minusDays(20);
//            TransactionFilterParams filterParams =
//                    new TransactionFilterParams();
//            filterParams.setSince(since);
//            filterParams.setTo(to);
//
//            // when
//            Long count = transactionDao.getCount(
//                    filterParams,
//                    userId
//            );
//
//            // then
//            assertThat(count).isNotNull();
//            assertThat(count).isEqualTo(expectedNumberOfRecords);
//        }

        @Test
        @Order(7)
        @DisplayName("should return user transactions count for specified accounts when accounts exist and belong to user")
        public void shouldReturnUserTransactionsCountForSpecifiedAccounts_whenAccountsExistsAndBelongToUser() {

            // given
            Long userId = 1L;
            List<Long> userAccountId = List.of(1L, 2L);
            Long expectedNumberOfRecords = 9L;
            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setAccountIds(userAccountId);

            // when
            Long count = transactionDao.getCount(
                    filterParams,
                    userId
            );

            // then
            assertThat(count).isNotNull();
            assertThat(count).isEqualTo(expectedNumberOfRecords);
        }

        @Test
        @Order(8)
        @DisplayName("should return zero transactions when user specified accounts that do not belong to him or don't exist")
        public void shouldReturnZeroTransactionsCount_whenUserSpecifiedAccountsThatDoNotBelongToHimOrDoNotExist() {

            // given
            Long userId = 1L;
            List<Long> userAccountId = List.of(4L, 100L);
            Long expectedNumberOfRecords = 0L;
            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setAccountIds(userAccountId);

            // when
            Long count = transactionDao.getCount(
                    filterParams,
                    userId
            );

            // then
            assertThat(count).isNotNull();
            assertThat(count).isEqualTo(expectedNumberOfRecords);
        }

        @Test
        @Order(9)
        @DisplayName("should return only user transactions when part of specified accounts do not belong to him or don't exist")
        public void shouldReturnOnlyUserTransactions_whenPartOfAccountsDoNotBelongToHimOrDoNotExist() {

            // given
            Long userId = 4L;
            List<Long> userAccountId = List.of(1L, 4L, 7L, 10L, 13L, 100L);
            Long expectedNumberOfRecords = 8L;
            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setAccountIds(userAccountId);

            // when
            Long count = transactionDao.getCount(
                    filterParams,
                    userId
            );

            // then
            assertThat(count).isNotNull();
            assertThat(count).isEqualTo(expectedNumberOfRecords);
        }

        @Test
        @Order(10)
        @DisplayName("should return user transactions count for specified categories when categories exist and belong to user")
        public void shouldReturnUserTransactionsCountForSpecifiedCategories_whenCategoriesExistsAndBelongToUser() {

            // given
            Long userId = 2L;
            List<Long> userCategoryIds = List.of(5L, 6L);
            Long expectedNumberOfRecords = 7L;
            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setCategoryIds(userCategoryIds);

            // when
            Long count = transactionDao.getCount(
                    filterParams,
                    userId
            );

            // then
            assertThat(count).isNotNull();
            assertThat(count).isEqualTo(expectedNumberOfRecords);
        }

        @Test
        @Order(11)
        @DisplayName("should return zero transactions count when user specified categories that do not belong to him or don't exist")
        public void shouldReturnZeroTransactionsCount_whenUserSpecifiedCategoriesThatDoNotBelongToHimOrDoNotExist() {

            // given
            Long userId = 3L;
            List<Long> userCategoryIds = List.of(2L, 5L, 14L, 100L);
            Long expectedNumberOfRecords = 0L;
            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setCategoryIds(userCategoryIds);

            // when
            Long count = transactionDao.getCount(
                    filterParams,
                    userId
            );

            // then
            assertThat(count).isNotNull();
            assertThat(count).isEqualTo(expectedNumberOfRecords);
        }

        @Test
        @Order(12)
        @DisplayName("should return only user transactions when part of specified categories do not belong to him or don't exist")
        public void shouldReturnOnlyUserTransactions_whenPartOfCategoriesDoNotBelongToHimOrDoNotExist() {

            // given
            Long userId = 4L;
            List<Long> userCategoryIds = List.of(2L, 6L, 9L, 15L, 100L);
            Long expectedNumberOfRecords = 3L;
            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setCategoryIds(userCategoryIds);

            // when
            Long count = transactionDao.getCount(
                    filterParams,
                    userId
            );

            // then
            assertThat(count).isNotNull();
            assertThat(count).isEqualTo(expectedNumberOfRecords);
        }
    }

    @Nested
    @DisplayName("getTuples() method tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetTuplesTests {

        @Test
        @Order(1)
        @DisplayName("should return user transactions first page when default filters are applied")
        public void shouldReturnUserTransactionsFirstPage_whenDefaultFiltersAndPaginationRulesApplied() {

            // given
            Long userId = 1L;

            // when
            List<Tuple> transactions = transactionDao.getTuples(
                    new TransactionPaginationParams(),
                    new TransactionFilterParams(),
                    userId
            );

            // then
            assertThat(transactions).isNotNull();
            assertThat(transactions.size()).isNotZero();
            assertThat(transactions.size()).isEqualTo(DEFAULT_PAGE_LIMIT);
            assertThat(transactions)
                    .isSortedAccordingTo(
                            Comparator.comparing((Tuple tuple) -> tuple.get("transactionDate", LocalDateTime.class)
                            ).reversed()
                    );
        }

        @Test
        @Order(2)
        @DisplayName("should return user transactions second page when default filters are applied")
        public void shouldReturnUserTransactionsSecondPage_whenDefaultFiltersAndPaginationRulesApplied() {

            // given
            Long userId = 1L;
            int expectedNumberOfRecords = 1;

            TransactionPaginationParams paginationParams =
                    new TransactionPaginationParams();
            paginationParams.setPage(2);

            // when
            List<Tuple> transactions = transactionDao.getTuples(
                    paginationParams,
                    new TransactionFilterParams(),
                    userId
            );

            // then
            assertThat(transactions).isNotNull();
            assertThat(transactions.size()).isNotZero();
            assertThat(transactions.size()).isEqualTo(expectedNumberOfRecords);
            assertThat(transactions)
                    .isSortedAccordingTo(
                            Comparator.comparing((Tuple tuple) -> tuple.get("transactionDate", LocalDateTime.class)
                            ).reversed()
                    );
        }

        @Test
        @Order(3)
        @DisplayName("should return user transactions page sorted ascending by date")
        public void shouldReturnUserTransactionsFirstPageSortedAscendingByDate() {

            // given
            Long userId = 1L;
            TransactionPaginationParams paginationParams =
                    new TransactionPaginationParams();
            paginationParams.setSortDirection(SortDirection.ASC);

            // when
            List<Tuple> transactions = transactionDao.getTuples(
                    paginationParams,
                    new TransactionFilterParams(),
                    userId
            );

            // then
            assertThat(transactions).isNotNull();
            assertThat(transactions.size()).isNotZero();
            assertThat(transactions.size()).isEqualTo(DEFAULT_PAGE_LIMIT);
            assertThat(transactions)
                    .isSortedAccordingTo(
                            Comparator.comparing((Tuple tuple) -> tuple.get("transactionDate", LocalDateTime.class)
                            )
                    );
        }

        @Test
        @Order(4)
        @DisplayName("should return user transactions page sorted ascending by amount")
        public void shouldReturnUserTransactionsFirstPageSortedAscendingByAmount() {

            // given
            Long userId = 1L;
            TransactionPaginationParams paginationParams =
                    new TransactionPaginationParams();
            paginationParams.setSortedBy(SortedBy.AMOUNT);
            paginationParams.setSortDirection(SortDirection.ASC);

            // when
            List<Tuple> transactions = transactionDao.getTuples(
                    paginationParams,
                    new TransactionFilterParams(),
                    userId
            );

            // then
            assertThat(transactions).isNotNull();
            assertThat(transactions.size()).isNotZero();
            assertThat(transactions.size()).isEqualTo(DEFAULT_PAGE_LIMIT);
            assertThat(transactions)
                    .isSortedAccordingTo(
                            Comparator.comparing((Tuple tuple) -> tuple.get("amount", BigDecimal.class)
                            )
                    );
        }

        @Test
        @Order(5)
        @DisplayName("should return user transactions page sorted descending by category name")
        public void shouldReturnUserTransactionsFirstPageSortedDescendingByCategoryName() {

            // given
            Long userId = 1L;
            TransactionPaginationParams paginationParams =
                    new TransactionPaginationParams();
            paginationParams.setSortedBy(SortedBy.CATEGORY);
            paginationParams.setSortDirection(SortDirection.DESC);

            // when
            List<Tuple> transactions = transactionDao.getTuples(
                    paginationParams,
                    new TransactionFilterParams(),
                    userId
            );

            // then
            assertThat(transactions).isNotNull();
            assertThat(transactions.size()).isNotZero();
            assertThat(transactions.size()).isEqualTo(DEFAULT_PAGE_LIMIT);
            assertThat(transactions)
                    .isSortedAccordingTo(
                            Comparator.comparing((Tuple tuple) -> tuple.get("categoryName", String.class)
                            ).reversed()
                    );
        }
    }

    @Nested
    @DisplayName("save() method tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SaveTests {

        @Test
        @Order(1)
        @DisplayName("should successfully save transaction and assign to category and account")
        public void shouldSuccessfullySaveTransactionWithRelations() {

            // when
            Long categoryId = 1L;
            Long accountId = 1L;

            Transaction transaction = new Transaction(
                    new BigDecimal("200"), "Książki", TransactionType.EXPENSE,
                    "Książki do szkoły", LocalDateTime.now()
            );

            Category category = em.find(Category.class, categoryId);
            Account account = em.find(Account.class, accountId);

            transaction.setCategory(category);
            transaction.setAccount(account);

            // when
            Transaction savedTransaction = transactionDao.save(transaction);
            em.clear();

            // then
            assertThat(savedTransaction).isNotNull();
            assertThat(savedTransaction.getId()).isGreaterThan(0L);

            Transaction newFetchedTransaction = em.find(Transaction.class, savedTransaction.getId());
            assertThat(newFetchedTransaction).isNotNull();
            assertThat(newFetchedTransaction.getCategory()).isNotNull();
            assertThat(newFetchedTransaction.getCategory().getId()).isEqualTo(categoryId);
            assertThat(newFetchedTransaction.getAccount()).isNotNull();
            assertThat(newFetchedTransaction.getAccount().getId()).isEqualTo(accountId);
        }
    }

    @Nested
    @DisplayName("delete() method tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteTests {

        @Test
        @Order(1)
        @DisplayName("should delete transaction without related category and account")
        public void shouldDeleteTransactionOnlyWithoutRelatedCategoryAndAccount() {

            // given
            Long transactionId = 22L;
            Transaction transactionToDelete = em.find(Transaction.class, transactionId);
            Long accountId = transactionToDelete.getAccount().getId();
            Long categoryId = transactionToDelete.getCategory().getId();

            // when
            transactionDao.delete(transactionToDelete);
            em.flush();
            em.clear();

            // then
            Transaction transaction = em.find(Transaction.class, transactionId);
            assertThat(transaction).isNull();

            Category category = em.find(Category.class, categoryId);
            assertThat(category).isNotNull();

            Account account = em.find(Account.class, accountId);
            assertThat(account).isNotNull();

        }
    }

    @Nested
    @DisplayName("indByTransactionIdAndUserId() method tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FindByTransactionIdAndUserIdTests {

        @Test
        @Order(1)
        @DisplayName("should find user transaction by user id and transaction id when exists")
        public void shouldFindUserTransactionByUserIdAndTransactionId_whenItExists() {

            // given
            Long userId = 1L;
            Long transactionId = 22L;

            // when
            Optional<Transaction> optTransaction = transactionDao.findByIdAndUserId(transactionId, userId);

            // then
            assertThat(optTransaction).isPresent();
        }

        @Test
        @Order(2)
        @DisplayName("should find no transaction when transaction do not belong to user")
        public void shouldFindNoTransaction_whenTransactionDoNotBelongToUser() {

            // given
            Long userId = 1L;
            Long transactionId = 30L;

            // when
            Optional<Transaction> optTransaction = transactionDao.findByIdAndUserId(transactionId, userId);

            // then
            assertThat(optTransaction).isEmpty();
        }
    }
}
