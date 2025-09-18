package com.example.budget_management_app.transaction.unit;

import com.example.budget_management_app.transaction.dao.TransactionDao;
import com.example.budget_management_app.transaction.domain.SortDirection;
import com.example.budget_management_app.transaction.domain.SortedBy;
import com.example.budget_management_app.transaction.domain.TransactionModeFilter;
import com.example.budget_management_app.transaction.domain.TransactionTypeFilter;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TransactionDao Unit Tests")
public class DaoTests {

    @Autowired
    private TransactionDao transactionDao;

    @Test
    @Order(1)
    public void getTransactionsCountWithDefaultFiltersTest() {

        long expectedNumberOfRecords = 31;
        Long count = transactionDao.getTransactionsCount(
                TransactionTypeFilter.ALL,
                TransactionModeFilter.ALL,
                List.of(1l, 2l, 3l, 4l),
                LocalDate.of(2025,9,1),
                null
                );

        assertThat(count).isNotNull();
        assertThat(count).isNotZero();
        assertThat(count).isEqualTo(expectedNumberOfRecords);
    }

    @Test
    @Order(2)
    public void getTransactionsCountWithCustomFiltersTest() {

        long expectedNumberOfRecords = 15;
        Long count = transactionDao.getTransactionsCount(
                TransactionTypeFilter.EXPENSE,
                TransactionModeFilter.ALL,
                List.of(1l, 2l),
                LocalDate.of(2025, 9, 5),
                LocalDate.of(2025,9,28)
        );

        assertThat(count).isNotNull();
        assertThat(count).isNotZero();
        assertThat(count).isEqualTo(expectedNumberOfRecords);
    }

    @Test
    @Order(3)
    public void getTransactionTuplesFirstPageWithDefaultFiltersTest() {

        int page = 1;
        int limit = 8;
        int expectedSize = 8;
        List<Tuple> transactions = transactionDao.getTransactions(
                page,
                limit,
                TransactionTypeFilter.ALL,
                TransactionModeFilter.ALL,
                List.of(1l, 2l, 3l, 4l, 5l),
                LocalDate.of(2025, 9, 1),
                null,
                SortedBy.DATE,
                SortDirection.DESC);

        assertThat(transactions).isNotNull();
        assertThat(transactions.size()).isEqualTo(expectedSize);
        transactions.forEach(System.out::println);
    }

    @Test
    @Order(4)
    public void getTransactionTuplesLastPageWithDefaultFiltersTest() {

        int page = 4;
        int limit = 8;
        int expectedSize = 7;
        List<Tuple> transactions = transactionDao.getTransactions(
                page,
                limit,
                TransactionTypeFilter.ALL,
                TransactionModeFilter.ALL,
                List.of(1l, 2l, 3l, 4l, 5l),
                LocalDate.of(2025, 9, 1),
                null,
                SortedBy.DATE,
                SortDirection.DESC);

        assertThat(transactions).isNotNull();
        assertThat(transactions.size()).isEqualTo(expectedSize);
        transactions.forEach(System.out::println);
    }

    @Test
    @Order(5)
    public void getTransactionFirstPageTuplesWithCustomFiltersTest() {

        int page = 1;
        int limit = 8;
        int expectedSize = 7;

        List<Tuple> transactions = transactionDao.getTransactions(
                page,
                limit,
                TransactionTypeFilter.EXPENSE,
                TransactionModeFilter.ALL,
                List.of(2l),
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 30),
                SortedBy.AMOUNT,
                SortDirection.DESC
        );

        assertThat(transactions).isNotNull();
        assertThat(transactions.size()).isNotZero();
        assertThat(transactions.size()).isEqualTo(expectedSize);

        transactions.forEach(System.out::println);
    }
}
