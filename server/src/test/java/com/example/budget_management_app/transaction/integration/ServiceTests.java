package com.example.budget_management_app.transaction.integration;

import com.example.budget_management_app.transaction.dao.TransactionDao;
import com.example.budget_management_app.transaction.domain.SortDirection;
import com.example.budget_management_app.transaction.domain.SortedBy;
import com.example.budget_management_app.transaction.domain.TransactionModeFilter;
import com.example.budget_management_app.transaction.domain.TransactionTypeFilter;
import com.example.budget_management_app.transaction.dto.PagedResponse;
import com.example.budget_management_app.transaction.dto.TransactionView;
import com.example.budget_management_app.transaction.service.TransactionService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TransactionService Integration Tests")
public class ServiceTests {

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private TransactionService transactionService;

    @Test
    @Order(1)
    public void getTransactionPageFirstPageWithDefaultFiltersTest() {

        int page = 1;
        int limit = 8;
        PagedResponse<TransactionView> transactionsPage = transactionService.getTransactionViews(
                page,
                limit,
                TransactionTypeFilter.ALL,
                TransactionModeFilter.ALL,
                List.of(1l, 2l, 3l, 4l),
                List.of(1l, 2l),
                LocalDate.of(2025,9,1),
                null,
                SortedBy.DATE,
                SortDirection.DESC,
                1l
        );
    }
}
