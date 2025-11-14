package com.example.budget_management_app.transaction;

import com.example.budget_management_app.transaction.dao.TransactionDao;
import com.example.budget_management_app.transaction.dao.TransactionDaoImpl;
import com.example.budget_management_app.transaction.dto.TransactionFilterParams;
import com.example.budget_management_app.transaction.dto.TransactionPaginationParams;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TransactionDaoImpl.class)
@DisplayName("TransactionDao Integration Test")
public class TransactionDaoIntegrationTest {

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private TestEntityManager em;

    private static final int DEFAULT_PAGE_SIZE = 8;

    @Nested
    @Sql("/sql/transactions-test.sql")
    @DisplayName("Testing getTuples() method")
    class GetTuplesTests {

        @Test
        void shouldReturnCorrectTuplesPage_whenFilteredByAllUserAccountsAndCategories() {

            // given
            List<Long> allAccountIds = em
                    .getEntityManager()
                    .createQuery("SELECT a.id FROM Account a", Long.class)
                    .getResultList();

            List<Long> allCategoryIds = em
                    .getEntityManager()
                    .createQuery("SELECT c.id FROM Category c", Long.class)
                    .getResultList();

            // Sanity check
            assertThat(allAccountIds).containsExactlyInAnyOrder(1L, 2L, 3L);
            assertThat(allCategoryIds).containsExactlyInAnyOrder(1L, 2L, 3L);

            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setAccountIds(allAccountIds);
            filterParams.setCategoryIds(allCategoryIds);

            // when
            List<Tuple> result = transactionDao.getTuples(
                    new TransactionPaginationParams(),
                    filterParams
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(DEFAULT_PAGE_SIZE);
        }

        @Test
        void shouldReturnCorrectTuplesPage_whenFilteredBySpecificUserAccountsAndCategories() {

            // given
            List<Long> allAccountIds = em
                    .getEntityManager()
                    .createQuery("SELECT a.id FROM Account a", Long.class)
                    .getResultList()
                    .stream()
                    .sorted()
                    .limit(1)
                    .toList();

            List<Long> allCategoryIds = em
                    .getEntityManager()
                    .createQuery("SELECT c.id FROM Category c", Long.class)
                    .getResultList()
                    .stream()
                    .sorted()
                    .limit(1)
                    .toList();

            // Sanity check
            assertThat(allAccountIds).containsExactlyInAnyOrder(1L);
            assertThat(allCategoryIds).containsExactlyInAnyOrder(1L);

            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setAccountIds(allAccountIds);
            filterParams.setCategoryIds(allCategoryIds);

            // when
            List<Tuple> result = transactionDao.getTuples(
                    new TransactionPaginationParams(),
                    filterParams
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.size()).isBetween(0, DEFAULT_PAGE_SIZE);
        }

        @Test
        void shouldReturnCorrectTuplesPage_whenSpecificFiltersAreApplied() {

            // given
            TransactionFilterParams filterParams =
                    new TransactionFilterParams();
            filterParams.setAccountIds(List.of(1L, 2L, 3L));
            filterParams.setCategoryIds(List.of(1L, 2L, 3L));



            // when
            List<Tuple> result = transactionDao.getTuples(
                    new TransactionPaginationParams(),
                    filterParams
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.size()).isBetween(0, DEFAULT_PAGE_SIZE);
        }
    }
//
//    @Test
//    @Order(3)
//    public void getTransactionsCountForRegularTransactionsTest() {
//
//        long expectedNumberOfRecords = 8L;
//        List<Long> allUserAccountsIds = List.of(1L, 2L, 3L);
//        List<Long> allUserCategoriesIds = List.of(1L, 2L, 3L, 4L);
//
//        TransactionFilterParams searchCriteria =
//                new TransactionFilterParams(
//                        TransactionTypeFilter.ALL,
//                        TransactionModeFilter.REGULAR,
//                        allUserAccountsIds,
//                        allUserCategoriesIds,
//                        LocalDate.of(2025, 9, 1),
//                        null
//                );
//
//        Long count = transactionDao.getCount(searchCriteria);
//
//        assertThat(count).isNotNull();
//        assertThat(count).isNotZero();
//        assertThat(count).isEqualTo(expectedNumberOfRecords);
//    }
//
//    @Test
//    @Order(4)
//    public void getRegularTransactionsCountForParticularAccountAndCategoriesTest() {
//
//        long expectedNumberOfRecords = 3L;
//        List<Long> allUserAccountsIds = List.of(1L);
//        List<Long> allUserCategoriesIds = List.of(2L, 4L);
//
//        TransactionFilterParams searchCriteria =
//                new TransactionFilterParams(
//                        TransactionTypeFilter.ALL,
//                        TransactionModeFilter.REGULAR,
//                        allUserAccountsIds,
//                        allUserCategoriesIds,
//                        LocalDate.of(2025, 9, 1),
//                        null
//                );
//
//        Long count = transactionDao.getCount(searchCriteria);
//
//        assertThat(count).isNotNull();
//        assertThat(count).isNotZero();
//        assertThat(count).isEqualTo(expectedNumberOfRecords);
//    }
//
//    @Test
//    @Order(5)
//    public void getRecurringTransactionsCountForParticularAccountAndCategoryTest() {
//
//        long expectedNumberOfRecords = 4L;
//        List<Long> allUserAccountsIds = List.of(1L);
//        List<Long> allUserCategoriesIds = List.of(4L);
//
//        TransactionFilterParams searchCriteria =
//                new TransactionFilterParams(
//                        TransactionTypeFilter.ALL,
//                        TransactionModeFilter.RECURRING,
//                        allUserAccountsIds,
//                        allUserCategoriesIds,
//                        LocalDate.of(2025, 9, 1),
//                        null
//                );
//
//        Long count = transactionDao.getCount(searchCriteria);
//
//        assertThat(count).isNotNull();
//        assertThat(count).isNotZero();
//        assertThat(count).isEqualTo(expectedNumberOfRecords);
//    }
//
//    @Test
//    @Order(6)
//    public void getTransactionTuplesFirstPageWithDefaultFiltersTest() {
//
//        int page = 1;
//        int limit = 5;
//        int expectedSize = 5;
//        List<Long> allUserAccountsIds = List.of(1L, 2L, 3L);
//        List<Long> allUserCategoriesIds = List.of(1L, 2L, 3L, 4L);
//
//        TransactionFilterParams searchCriteria =
//                new TransactionFilterParams(
//                        TransactionTypeFilter.ALL,
//                        TransactionModeFilter.ALL,
//                        allUserAccountsIds,
//                        allUserCategoriesIds,
//                        LocalDate.of(2025, 9, 1),
//                        null
//                );
//
//        TransactionPaginationParams pageReq =
//                new TransactionPaginationParams(page, limit, SortedBy.DATE, SortDirection.DESC);
//
//        List<Tuple> transactions = transactionDao.getTuples(pageReq, searchCriteria);
//
//        assertThat(transactions).isNotNull();
//        assertThat(transactions.size()).isNotZero();
//        assertThat(transactions.size()).isEqualTo(expectedSize);
//
//        transactions.forEach(System.out::println);
//    }
//
//    @Test
//    @Order(7)
//    public void getTransactionTuplesLastPageForAllUserTransactionsTest() {
//
//        int page = 3;
//        int limit = 5;
//        int expectedSize = 2;
//        List<Long> allUserAccountsIds = List.of(1L, 2L, 3L);
//        List<Long> allUserCategoriesIds = List.of(1L, 2L, 3L, 4L);
//
//        TransactionFilterParams searchCriteria =
//                new TransactionFilterParams(
//                        TransactionTypeFilter.ALL,
//                        TransactionModeFilter.ALL,
//                        allUserAccountsIds,
//                        allUserCategoriesIds,
//                        LocalDate.of(2025, 9, 1),
//                        null
//                );
//
//        TransactionPaginationParams pageReq =
//                new TransactionPaginationParams(page, limit, SortedBy.DATE, SortDirection.DESC);
//
//        List<Tuple> transactions = transactionDao.getTuples(pageReq, searchCriteria);
//
//        assertThat(transactions).isNotNull();
//        assertThat(transactions.size()).isNotZero();
//        assertThat(transactions.size()).isEqualTo(expectedSize);
//
//        transactions.forEach(System.out::println);
//    }
//
//    @Test
//    @Order(8)
//    public void getAllExpenseTypeTransactionTuplesFirstPageForCustomDataAndTest() {
//
//        int page = 1;
//        int limit = 5;
//        int expectedSize = 5;
//        List<Long> allUserAccountsIds = List.of(1L, 2L, 3L);
//        List<Long> allUserCategoriesIds = List.of(1L, 2L, 3L, 4L);
//
//        TransactionFilterParams searchCriteria =
//                new TransactionFilterParams(
//                        TransactionTypeFilter.EXPENSE,
//                        TransactionModeFilter.ALL,
//                        allUserAccountsIds,
//                        allUserCategoriesIds,
//                        LocalDate.of(2025, 10, 1),
//                        LocalDate.of(2025,10, 21)
//                );
//
//        TransactionPaginationParams pageReq =
//                new TransactionPaginationParams(page, limit, SortedBy.DATE, SortDirection.DESC);
//
//        List<Tuple> transactions = transactionDao.getTuples(pageReq, searchCriteria);
//
//        assertThat(transactions).isNotNull();
//        assertThat(transactions.size()).isNotZero();
//        assertThat(transactions.size()).isEqualTo(expectedSize);
//
//        transactions.forEach(System.out::println);
//    }
//
//    @Test
//    @Order(9)
//    public void getAllExpenseTypeTransactionTuplesLastPageForCustomDataAndTest() {
//
//        int page = 2;
//        int limit = 5;
//        int expectedSize = 2;
//        List<Long> allUserAccountsIds = List.of(1L, 2L, 3L);
//        List<Long> allUserCategoriesIds = List.of(1L, 2L, 3L, 4L);
//
//        TransactionFilterParams searchCriteria =
//                new TransactionFilterParams(
//                        TransactionTypeFilter.EXPENSE,
//                        TransactionModeFilter.ALL,
//                        allUserAccountsIds,
//                        allUserCategoriesIds,
//                        LocalDate.of(2025, 10, 1),
//                        LocalDate.of(2025,10, 21)
//                );
//
//        TransactionPaginationParams pageReq =
//                new TransactionPaginationParams(page, limit, SortedBy.DATE, SortDirection.DESC);
//
//        List<Tuple> transactions = transactionDao.getTuples(pageReq, searchCriteria);
//
//        assertThat(transactions).isNotNull();
//        assertThat(transactions.size()).isNotZero();
//        assertThat(transactions.size()).isEqualTo(expectedSize);
//
//        transactions.forEach(System.out::println);
//    }
//
//    @Test
//    @Order(10)
//    public void saveRegularTransactionTest() {
//
//        long expectedIdValue = 61L;
//        long categoryId = 1L;
//        long accountId = 1L;
//
//        Transaction transaction = new Transaction(
//                new BigDecimal("200"), "Książki", TransactionType.EXPENSE,
//                "Książki do szkoły", LocalDateTime.now()
//        );
//
//        Category category = em.find(Category.class, categoryId);
//        assertThat(category).isNotNull();
//
//        Account account = em.find(Account.class, accountId);
//        assertThat(account).isNotNull();
//
//        transaction.setCategory(category);
//        transaction.setAccount(account);
//
//        Transaction savedTransaction = transactionDao.save(transaction);
//        assertThat(savedTransaction).isNotNull();
//        assertThat(savedTransaction.getId()).isNotNull();
//        assertThat(savedTransaction.getId()).isEqualTo(expectedIdValue);
//
//        em.clear();
//
//        Transaction newFetchedTransaction = em.find(Transaction.class, savedTransaction.getId());
//        assertThat(newFetchedTransaction).isNotNull();
//        assertThat(newFetchedTransaction.getCategory()).isNotNull();
//        assertThat(newFetchedTransaction.getCategory().getId()).isEqualTo(categoryId);
//        assertThat(newFetchedTransaction.getAccount()).isNotNull();
//        assertThat(newFetchedTransaction.getAccount().getId()).isEqualTo(accountId);
//
//        System.out.println(newFetchedTransaction);
//    }
//
//    @Test
//    @Order(11)
//    public void deleteRegularTransactionTest() {
//
//        long transactionId = 28L;
//        Transaction transactionToDelete = em.find(Transaction.class, transactionId);
//        transactionDao.delete(transactionToDelete);
//
//        em.flush();
//        em.clear();
//
//        Transaction transaction = em.find(Transaction.class, transactionId);
//        assertThat(transaction).isNull();
//
//    }
//
//    @Test
//    @Order(12)
//    public void findTransactionsByIdAndUserId() {
//
//        long userId = 1;
//        long transactionId = 26L;
//        long expectedAccountId = 1L;
//        long expectedCategoryId = 4L;
//
//        Optional<Transaction> optTransaction = transactionDao.findByIdAndUserId(transactionId, userId);
//        assertThat(optTransaction).isPresent();
//
//        Transaction transaction = optTransaction.get();
//        assertThat(transaction.getId()).isEqualTo(transactionId);
//        assertThat(transaction.getAccount().getId()).isEqualTo(expectedAccountId);
//        assertThat(transaction.getCategory().getId()).isEqualTo(expectedCategoryId);
//        assertThat(transaction.getAccount().getUser().getId()).isEqualTo(userId);
//
//    }
//
//    @Test
//    @Order(13)
//    public void findRecurringTransactionByRecurringTemplateId() {
//
//        long recurringTemplateId = 1L;
//        long expectedValue = 2L;
//
//        List<Transaction> result = transactionDao.findByRecurringTransactionId(recurringTemplateId);
//        assertThat(result).isNotNull();
//        assertThat(result.size()).isNotZero();
//        assertThat(result.size()).isEqualTo(expectedValue);
//
//        result.forEach(System.out::println);
//    }
}
