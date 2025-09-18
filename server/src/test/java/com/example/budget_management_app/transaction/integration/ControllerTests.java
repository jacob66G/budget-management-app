package com.example.budget_management_app.transaction.integration;

import com.example.budget_management_app.transaction.controller.TransactionController;
import com.example.budget_management_app.transaction.dao.TransactionDao;
import com.example.budget_management_app.transaction.dto.PagedResponse;
import com.example.budget_management_app.transaction.service.TransactionService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TransactionController Integration Tests")
public class ControllerTests {

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionController transactionController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    public void getTransactionPageWithDefaultFiltersTest() {

        String requestUrl = "/api/v1/transactions?page=1&limit=8&accounts=1,2,3&since=2025-09-01";

        ResponseEntity<PagedResponse> response = restTemplate.getForEntity(
                requestUrl, PagedResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().pagination()).isNotNull();
        System.out.println("-------------------------");
        System.out.println(response.getBody());

    }

    @Test
    @Order(2)
    public void getTransactionPageWithCustomFiltersTest() {

        String requestUrl = "/api/v1/transactions?page=1&limit=8&type=EXPENSE&mode=REGULAR&accounts=1,2" +
                "&since=2025-09-01&to=2025-09-25&sortBy=AMOUNT&sortDirection=ASC";

        ResponseEntity<PagedResponse> response = restTemplate.getForEntity(
                requestUrl, PagedResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().pagination()).isNotNull();
        System.out.println("-------------------------");
        System.out.println(response.getBody());
    }
}
