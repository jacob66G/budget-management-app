package com.example.budget_management_app.transaction.service;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.common.exception.CategoryChangeNotAllowedException;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.TransactionTypeMismatchException;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction.dto.*;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@Sql("/sql/transactions-test-data.sql")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TransactionService Integration Tests")
public class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private EntityManager em;

    private final int DEFAULT_PAGE_LIMIT = 8;

    @Test
    @Order(1)
    public void shouldReturnUserTransactionFirstPage() {

        // given
        Long userId = 1L;
        TransactionPaginationParams paginationParams =
                new TransactionPaginationParams();

        // when
        PagedResponse<TransactionSummary> transactionsPage = transactionService.getSummariesPage(
                paginationParams,
                new TransactionFilterParams(),
                userId
        );

        // then
        assertThat(transactionsPage).isNotNull();
        assertThat(transactionsPage.pagination()).isNotNull();
        assertThat(transactionsPage.data()).isNotNull();
        assertThat(transactionsPage.links()).isNull();
        assertThat(transactionsPage.data().size()).isEqualTo(DEFAULT_PAGE_LIMIT);
    }

    @Test
    @Order(2)
    public void shouldReturnTransactionLastPage() {

        // given
        Long userId = 1L;
        TransactionPaginationParams paginationParams =
                new TransactionPaginationParams();
        paginationParams.setPage(2);

        // when
        PagedResponse<TransactionSummary> transactionsPage = transactionService.getSummariesPage(
                paginationParams,
                new TransactionFilterParams(),
                userId
        );

        // then
        assertThat(transactionsPage).isNotNull();
        assertThat(transactionsPage.pagination()).isNotNull();
        assertThat(transactionsPage.data()).isNotNull();
        assertThat(transactionsPage.data().size()).isEqualTo(1);
    }

    @Test
    @Order(3)
    public void shouldThrowNotFoundException_whenSpecifiedAccountAreNotUser() {

        // given
        Long userId = 1L;
        List<Long> requestedAccountIds = List.of(1L, 2L, 3L, 4L);
        TransactionFilterParams filterParams =
                new TransactionFilterParams();
        filterParams.setAccountIds(requestedAccountIds);

        // when and then
        assertThrows(NotFoundException.class, () -> {
            transactionService.getSummariesPage(
                    new TransactionPaginationParams(),
                    filterParams,
                    userId
            );
        });
    }

    @Test
    @Order(4)
    public void shouldThrowNotFoundException_whenSpecifiedCategoriesAreNotUser() {

        // given
        Long userId = 1L;
        List<Long> requestedCategoryId = List.of(5L, 6L, 3L);
        TransactionFilterParams filterParams =
                new TransactionFilterParams();
        filterParams.setCategoryIds(requestedCategoryId);

        // when and then
        assertThrows(NotFoundException.class, () -> {
            transactionService.getSummariesPage(
                    new TransactionPaginationParams(),
                    filterParams,
                    userId
            );
        });
    }

    @Test
    @Order(5)
    public void shouldThrowNotFoundException_whenCategoryDoesNotBelongToUser() {

        // given
        Long userId = 1L;
        Long categoryId = 5L;
        Long accountId = 1L;

        TransactionCreateRequest createReq =
                new TransactionCreateRequest(
                        new BigDecimal("250"), "Książki", TransactionType.EXPENSE,
                        "Nowe książki do szkoły", accountId, categoryId
                );

        // when and then
        assertThrows(NotFoundException.class, () -> {
            transactionService.create(createReq, userId);
        });
    }

    @Test
    @Order(6)
    public void shouldThrowNotFoundException_whenAccountDoesNotBelongToUser() {

        // given
        Long userId = 1L;
        Long categoryId = 1L;
        Long accountId = 4L;

        TransactionCreateRequest createReq =
                new TransactionCreateRequest(
                        new BigDecimal("200"), "Obiad w pogodnym", TransactionType.EXPENSE,
                        null, accountId, categoryId
                );

        // when and then
        assertThrows(NotFoundException.class, () -> {
            transactionService.create(createReq, userId);
        });
    }

    @Test
    @Order(7)
    @Transactional
    public void shouldCreateExpenseTypeTransactions() {

        // given
        Long userId = 2L;
        Long categoryId = 6L;
        Long accountId = 4L;

        TransactionCreateRequest createReq =
                new TransactionCreateRequest(
                        new BigDecimal("800.00"), "Nowy materac", TransactionType.EXPENSE,
                        "", accountId, categoryId
                );
        // when
        TransactionCreateResponse response = transactionService.create(createReq, userId);
        em.flush();
        em.clear();

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isGreaterThan(0L);

        Transaction createdTransaction = em.find(Transaction.class, response.id());
        assertThat(createdTransaction).isNotNull();

        Account assignedAccount = createdTransaction.getAccount();
        assertThat(assignedAccount).isNotNull();
        assertThat(assignedAccount.getId()).isEqualTo(accountId);
        assertThat(assignedAccount.getBalance()).isEqualTo(new BigDecimal("2200.00"));
        assertThat(assignedAccount.getTotalExpense()).isEqualTo(new BigDecimal("2499.50"));

        assertThat(createdTransaction.getCategory().getId()).isEqualTo(categoryId);
    }

    @Test
    @Order(8)
    @Transactional
    public void shouldCreateIncomeTypeTransactionTest() {

        // given
        Long userId = 1L;
        Long categoryId = 3L;
        Long accountId = 1L;

        TransactionCreateRequest createReq =
                new TransactionCreateRequest(
                        new BigDecimal("800.00"), "Premia", TransactionType.INCOME,
                        "", accountId, categoryId
                );

        // when
        TransactionCreateResponse response = transactionService.create(createReq, userId);
        em.flush();
        em.clear();

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isGreaterThan(0L);

        Transaction createdTransaction = em.find(Transaction.class, response.id());
        assertThat(createdTransaction).isNotNull();

        Account assignedAccount = createdTransaction.getAccount();
        assertThat(assignedAccount).isNotNull();
        assertThat(assignedAccount.getId()).isEqualTo(accountId);
        assertThat(assignedAccount.getBalance()).isEqualTo(new BigDecimal("2300.00"));
        assertThat(assignedAccount.getTotalIncome()).isEqualTo(new BigDecimal("5800.00"));

        assertThat(createdTransaction.getCategory().getId()).isEqualTo(categoryId);
    }

    @Test
    @Order(9)
    public void shouldThrowTransactionTypeMismatchException_whenCategoryIsIncompatibleType() {

        // given
        Long userId = 2L;
        Long categoryId = 6L;
        Long accountId = 4L;

        TransactionCreateRequest createReq =
                new TransactionCreateRequest(
                        new BigDecimal("200"), "Urodziny", TransactionType.INCOME,
                        null, accountId, categoryId
                );

        // when and then
        assertThrows(TransactionTypeMismatchException.class, () -> {
            transactionService.create(createReq, userId);
        });
    }

    @Test
    @Order(10)
    public void shouldThrowCategoryChangeNotAllowedException_whenTransactionIsRecurring() {

        // given
        Long userId = 3L;
        Long transactionId = 9L;
        Long currentTransactionCategoryId = 8L;
        Long newTransactionCategoryId = 10L;
        Long accountId = 0L;

        TransactionCategoryChangeRequest updReq =
                new TransactionCategoryChangeRequest(
                        currentTransactionCategoryId,
                        newTransactionCategoryId,
                        accountId
                );

        // when and then
        assertThrows( CategoryChangeNotAllowedException.class, () -> {
            transactionService.changeCategory(transactionId, updReq, userId);
        });
    }

    @Test
    @Order(11)
    public void shouldThrowNotFoundException_whenTransactionBelongToOtherUser() {

        // given
        Long userId = 1L;
        Long transactionId = 30L;
        Long currentTransactionCategoryId = 6L;
        Long newTransactionCategoryId = 5L;
        Long accountId = 0L;

        TransactionCategoryChangeRequest updReq =
                new TransactionCategoryChangeRequest(
                        currentTransactionCategoryId,
                        newTransactionCategoryId,
                        accountId
                );

        // when and then
        assertThrows( NotFoundException.class, () -> {
            transactionService.changeCategory(transactionId, updReq, userId);
        });
    }

    @Test
    @Order(12)
    public void shouldThrowNotFoundException_whenTransactionDoesNotBelongToSpecifiedCategory() {

        Long userId = 1L;
        Long transactionId = 22L;
        Long currentTransactionCategoryId = 6L;
        Long newTransactionCategoryId = 1L;
        Long accountId = 0L;

        TransactionCategoryChangeRequest updReq =
                new TransactionCategoryChangeRequest(
                        currentTransactionCategoryId,
                        newTransactionCategoryId,
                        accountId
                );

        assertThrows( NotFoundException.class, () -> {
            transactionService.changeCategory(transactionId, updReq, userId);
        });
    }

    @Test
    @Order(14)
    @Transactional
    public void shouldChangeTransactionCategory() {

        // given
        Long userId = 1L;
        Long transactionId = 22L;
        Long currentTransactionCategoryId = 2L;
        Long newTransactionCategoryId = 4L;
        Long accountId = 0L;

        TransactionCategoryChangeRequest updReq =
                new TransactionCategoryChangeRequest(
                        currentTransactionCategoryId,
                        newTransactionCategoryId,
                        accountId
                );

        // when
        TransactionCategoryChangeResponse updateResponse =
                transactionService.changeCategory(
                        transactionId, updReq, userId
                );

        em.flush();
        em.clear();

        // then
        assertThat(updateResponse).isNotNull();
        assertThat(updateResponse.categoryId()).isEqualTo(newTransactionCategoryId);

        Transaction transaction = em.find(Transaction.class, transactionId);
        assertThat(transaction).isNotNull();

        assertThat(transaction.getCategory().getId()).isEqualTo(newTransactionCategoryId);
    }

    @Test
    @Order(15)
    public void shouldThrowTransactionTypeMismatchException_whenNewCategoryIsIncompatibleType() {

        // given
        Long userId = 1L;
        Long transactionId = 22L;
        Long currentTransactionCategoryId = 2L;
        Long newTransactionCategoryId = 3L;
        Long accountId = 0L;

        TransactionCategoryChangeRequest updReq =
                new TransactionCategoryChangeRequest(
                        currentTransactionCategoryId,
                        newTransactionCategoryId,
                        accountId
                );

        // when and then
        assertThrows(TransactionTypeMismatchException.class, () -> {
            transactionService.changeCategory(
                    transactionId, updReq, userId
            );
        });

    }

    @Test
    @Order(16)
    public void shouldThrowNotFoundException_whenTransactionIsNotUser() {

        Long userId = 2L;
        Long transactionId = 22L;

        TransactionUpdateRequest updateReq =
                new TransactionUpdateRequest(
                        "Bilet miesięczny", new BigDecimal("70"),
                        "Bilet miesięczny podrożał"
                );

        assertThrows(NotFoundException.class, () -> {
            transactionService.update(transactionId, updateReq, userId);
        });
    }

    @Test
    @Order(17)
    @Transactional
    public void shouldUpdateTransactionByIncreasingTransactionExpenseValueAndUpdateAccountDetails() {

        // given
        Long userId = 1L;
        Long transactionId = 22L;

        TransactionUpdateRequest updateReq =
                new TransactionUpdateRequest(
                        "Bilet miesięczny", new BigDecimal("70"),
                        "Bilet miesięczny podrożał"
                );

        // when
        transactionService.update(transactionId, updateReq, userId);
        em.flush();
        em.clear();

        // then
        Transaction updatedTransaction = em.find(Transaction.class, transactionId);
        assertThat(updatedTransaction).isNotNull();
        assertThat(updatedTransaction.getId()).isEqualTo(transactionId);

        Account account = updatedTransaction.getAccount();
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1480.00"));
        assertThat(account.getTotalExpense()).isEqualTo(new BigDecimal("3520.00"));
    }

    @Test
    @Order(18)
    @Transactional
    public void shouldUpdateTransactionByDecreasingTransactionExpenseValueAndUpdateAccountDetails() {

        // given
        Long userId = 1L;
        Long transactionId = 22L;

        TransactionUpdateRequest updateReq =
                new TransactionUpdateRequest(
                        "Bilet miesięczny", new BigDecimal("40"),
                        "Bilet miesięczny potaniał"
                );

        // when
        transactionService.update(transactionId, updateReq, userId);
        em.flush();
        em.clear();

        // then
        Transaction updatedTransaction = em.find(Transaction.class, transactionId);
        assertThat(updatedTransaction).isNotNull();
        assertThat(updatedTransaction.getId()).isEqualTo(transactionId);

        Account account = updatedTransaction.getAccount();
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1510.00"));
        assertThat(account.getTotalExpense()).isEqualTo(new BigDecimal("3490.00"));
    }

    @Test
    @Order(19)
    @Transactional
    public void shouldUpdateTransactionByIncreasingTransactionIncomeValueAndUpdateAccountDetails() {

        // given
        Long userId = 1L;
        Long transactionId = 23L;

        TransactionUpdateRequest updateReq =
                new TransactionUpdateRequest(
                        "Wypłata", new BigDecimal("5100"),
                        "Wypłata z premią - 100zł"
                );

        // when
        transactionService.update(transactionId, updateReq, userId);
        em.flush();
        em.clear();

        // then
        Transaction updatedTransaction = em.find(Transaction.class, transactionId);
        assertThat(updatedTransaction).isNotNull();
        assertThat(updatedTransaction.getId()).isEqualTo(transactionId);

        Account account = updatedTransaction.getAccount();
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1600.00"));
        assertThat(account.getTotalIncome()).isEqualTo(new BigDecimal("5100.00"));
    }

    @Test
    @Order(20)
    @Transactional
    public void shouldUpdateTransactionByDecreasingTransactionIncomeValueAndUpdateAccountDetails() {

        // given
        Long userId = 1L;
        Long transactionId = 23L;

        TransactionUpdateRequest updateReq =
                new TransactionUpdateRequest(
                        "Wypłata", new BigDecimal("4900"),
                        "Wypłata obcięta o 100zł"
                );

        // when
        transactionService.update(transactionId, updateReq, userId);
        em.flush();
        em.clear();


        // then
        Transaction updatedTransaction = em.find(Transaction.class, transactionId);
        assertThat(updatedTransaction).isNotNull();
        assertThat(updatedTransaction.getId()).isEqualTo(transactionId);

        Account account = updatedTransaction.getAccount();
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1400.00"));
        assertThat(account.getTotalIncome()).isEqualTo(new BigDecimal("4900.00"));
    }

    @Test
    @Order(21)
    public void shouldThrowNotFoundException_whenDeletingOtherUserTransaction() {

        // given
        Long userId = 2L;
        Long transactionId = 22L;

        // when and then
        assertThrows( NotFoundException.class, () -> {
            transactionService.delete(transactionId, userId);
        });
    }

    @Test
    @Order(22)
    public void shouldDeleteExpenseTypeTransactionAndUpdateAccountDetails() {

        // given
        Long userId = 1L;
        Long transactionId = 22L;
        Long accountId = 1L;

        // when
        transactionService.delete(transactionId, userId);

        // then
        Transaction deletedTransaction = em.find(Transaction.class, transactionId);
        assertThat(deletedTransaction).isNull();

        Account account = em.find(Account.class, accountId);
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1550.00"));
        assertThat(account.getTotalExpense()).isEqualTo(new BigDecimal("3450.00"));
    }

    @Test
    @Order(23)
    public void shouldDeleteIncomeTypeTransactionAndUpdateAccountDetails() {

        // given
        Long userId = 1L;
        Long transactionId = 28L;
        Long accountId = 2L;

        // when
        transactionService.delete(transactionId, userId);

        // then
        Transaction deletedTransaction = em.find(Transaction.class, transactionId);
        assertThat(deletedTransaction).isNull();

        Account account = em.find(Account.class, accountId);
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("9000.00"));
        assertThat(account.getTotalIncome()).isEqualTo(new BigDecimal("0.00"));
    }
}
