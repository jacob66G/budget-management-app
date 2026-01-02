package com.example.budget_management_app.recurring_transaction.service;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.StatusAlreadySetException;
import com.example.budget_management_app.common.exception.TransactionTypeMismatchException;
import com.example.budget_management_app.recurring_transaction.domain.*;
import com.example.budget_management_app.recurring_transaction.dto.*;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.transaction_common.dto.PaginationParams;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Disabled;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Sql("/sql/transactions-test-data.sql")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RecurringTransactionService Integration Tests")
@ActiveProfiles("test")
@Disabled
public class RecurringTransactionsServiceIntegrationTest {

    @Autowired
    private RecurringTransactionService recTransactionService;

    @Autowired
    private EntityManager em;

    private final int DEFAULT_PAGE_LIMIT = 5;

    @Test
    @Order(1)
    public void shouldReturnRecurringTransactionsPageForUser() {

        // given
        Long userId = 1L;
        int expectedTotalCount = 9;

        PaginationParams paginationParams =
                new PaginationParams();
        paginationParams.setLimit(DEFAULT_PAGE_LIMIT);

        // when
        PagedResponse<RecurringTransactionSummary> pagedResponse
                = recTransactionService.getSummariesPage(
                        paginationParams,
                        userId
        );

        // then
        assertThat(pagedResponse).isNotNull();
        assertThat(pagedResponse.data()).isNotNull();
        assertThat(pagedResponse.pagination()).isNotNull();
        assertThat(pagedResponse.data().size()).isEqualTo(DEFAULT_PAGE_LIMIT);
        assertThat(pagedResponse.pagination().totalCount()).isEqualTo(expectedTotalCount);
    }

    @Test
    @Order(2)
    public void shouldReturnRecurringTransactionTemplateDetailsTest() {

        // given
        Long userId = 1L;
        Long recTransactionId = 1L;

        // when
        RecurringTransactionDetailsResponse response =
                recTransactionService.getDetails(recTransactionId, userId);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @Order(3)
    public void shouldThrowNotFoundException_whenRecurringTransactionIsNotUser() {

        // given
        Long userId = 1L;
        Long recTransactionId = 3L;

        // when and then
        assertThrows( NotFoundException.class, () -> {
            recTransactionService.getDetails(recTransactionId, userId);
        });
    }

    @Test
    @Order(4)
    public void shouldThrowNotFoundException_whenRecurringTransactionDoesNotExist() {

        // given
        Long userId = 1L;
        Long recTransactionId = 15L;


        // when
        assertThrows( NotFoundException.class, () -> {
            recTransactionService.getDetails(recTransactionId, userId);
        });
    }

    @Test
    @Order(5)
    public void shouldCreateRecurringTransactionTemplateWhichStartsAfterCurrentDay() {

        // given
        Long userId = 1L;
        Long accountId = 1L;
        Long categoryId = 4L;

        RecurringTransactionCreateRequest createReq =
                new RecurringTransactionCreateRequest(
                    BigDecimal.valueOf(250L), "Orange", TransactionType.EXPENSE,
                    "Rachunek za telefon", LocalDate.now().plusDays(2), null,
                        RecurringInterval.MONTH, 1, accountId, categoryId
                );

        // when
        RecurringTransactionCreateResponse createResponse =
                recTransactionService.create(createReq, userId);
        em.clear();

        assertThat(createResponse).isNotNull();
        assertThat(createResponse.newTransaction()).isEqualTo(false);
        assertThat(createResponse.newTransactionView()).isNull();

        RecurringTransaction recurringTransaction = em.find(RecurringTransaction.class, createResponse.id());
        assertThat(createResponse.id()).isEqualTo(recurringTransaction.getId());
        assertThat(recurringTransaction.getCategory().getId()).isEqualTo(categoryId);
        assertThat(recurringTransaction.getAccount().getId()).isEqualTo(accountId);
    }

    @Test
    @Order(6)
    public void shouldCreateRecurringTransactionTemplateWhichStartsAtTheCurrentDay() {

        // given
        Long userId = 2L;
        Long accountId = 4L;
        Long categoryId = 6L;

        RecurringTransactionCreateRequest createReq =
                new RecurringTransactionCreateRequest(
                        BigDecimal.valueOf(1250L), "Czynsz", TransactionType.EXPENSE,
                        "Miesięczna opłata za czynsz", LocalDate.now(), null,
                        RecurringInterval.MONTH, 1, accountId, categoryId
                );

        // when
        RecurringTransactionCreateResponse createResponse =
                recTransactionService.create(createReq, userId);
        em.flush();
        em.clear();

        // then
        assertThat(createResponse).isNotNull();
        assertThat(createResponse.newTransaction()).isEqualTo(true);
        assertThat(createResponse.newTransactionView()).isNotNull();

        RecurringTransaction recurringTransaction = em.find(RecurringTransaction.class, createResponse.id());
        assertThat(createResponse.id()).isEqualTo(recurringTransaction.getId());
        assertThat(recurringTransaction.getCategory().getId()).isEqualTo(categoryId);
        assertThat(recurringTransaction.getAccount().getId()).isEqualTo(accountId);

        Optional<Transaction> optCreatedTransaction = recurringTransaction
                .getTransactions()
                .stream()
                .findFirst();

        assertThat(optCreatedTransaction).isPresent();
        Transaction createdTransaction = optCreatedTransaction.get();
        assertThat(createdTransaction.getAccount().getId()).isEqualTo(accountId);
        assertThat(createdTransaction.getCategory().getId()).isEqualTo(categoryId);

        Account account = em.find(Account.class, accountId);
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1750.00"));
        assertThat(account.getTotalExpense()).isEqualTo(new BigDecimal("2949.50"));
    }

    @Test
    @Order(7)
    @Disabled
    public void shouldCreateRecurringTransactionTemplateWhichStartsAtTheCurrentDayTest() {

        // given
        Long userId = 3L;
        Long accountId = 6L;
        Long categoryId = 12L;

        RecurringTransactionCreateRequest createReq =
                new RecurringTransactionCreateRequest(
                        BigDecimal.valueOf(250L), "Karnet na siłownie", TransactionType.EXPENSE,
                        "Miesięczny karnet na siłownie", LocalDate.now(), null,
                        RecurringInterval.MONTH, 1, accountId, categoryId
                );

        // when
        RecurringTransactionCreateResponse createResponse =
                recTransactionService.create(createReq, userId);
        em.clear();

        // then
        assertThat(createResponse).isNotNull();
//        assertThat(createResponse.newTransaction()).isEqualTo(false);
//        assertThat(createResponse.newTransactionView()).isNull();

        RecurringTransaction recurringTransaction = em.find(RecurringTransaction.class, createResponse.id());
        assertThat(createResponse.id()).isEqualTo(recurringTransaction.getId());
        assertThat(recurringTransaction.getCategory().getId()).isEqualTo(categoryId);
        assertThat(recurringTransaction.getAccount().getId()).isEqualTo(accountId);
    }

    @Test
    @Order(9)
    public void shouldThrowNotFoundException_whenAccountIsNotUser() {

        // given
        Long userId = 1L;
        Long accountId = 4L;
        Long categoryId = 6L;

        RecurringTransactionCreateRequest createReq =
                new RecurringTransactionCreateRequest(
                        BigDecimal.valueOf(1250L), "Czynsz", TransactionType.EXPENSE,
                        "Miesięczna opłata za czynsz", LocalDate.now(), null,
                        RecurringInterval.MONTH, 1, accountId, categoryId
                );

        // when and then
        assertThrows(NotFoundException.class, () -> {
            recTransactionService.create(createReq, userId);
        });
    }

    @Test
    @Order(10)
    public void createRecurringTransactionWithCategoryWithIncompatibleTypeTest() {

        // given
        Long userId = 2L;
        Long accountId = 4L;
        Long categoryId = 6L;

        RecurringTransactionCreateRequest createReq =
                new RecurringTransactionCreateRequest(
                        BigDecimal.valueOf(400), "Premia", TransactionType.INCOME,
                        null, LocalDate.now(), null,
                        RecurringInterval.MONTH, 1, accountId, categoryId
                );

        // when and then
        assertThrows(TransactionTypeMismatchException.class, () -> {
            recTransactionService.create(createReq, userId);
        });
    }

    @Test
    @Order(11)
    public void deleteRecurringTransactionTemplateWithoutTransactionsTest() {

        // given
        Long userId = 2L;
        Long recurringTransactionTemplateId = 3L;
        RemovalRange range = RemovalRange.TEMPLATE;

        // when
        recTransactionService.delete(recurringTransactionTemplateId, range, userId);
        em.flush();
        em.clear();

        // then
        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recurringTransactionTemplateId);
        assertThat(recTransaction).isNull();

        List<Long> detachedTransactionsIds = List.of(5L, 6L);
        List<Transaction> detachedTransactions = em.createQuery("""
                SELECT t FROM Transaction t
                WHERE t.id IN :detachedTransactionsIds
                """, Transaction.class)
                .setParameter("detachedTransactionsIds", detachedTransactionsIds)
                .getResultList();

        assertThat(detachedTransactions).isNotNull();
        assertThat(detachedTransactions.size()).isEqualTo(2);

        detachedTransactions.forEach(
                transaction -> {
                    assertThat(transaction.getRecurringTransaction()).isNull();
                }
        );
    }

    @Test
    @Order(12)
    public void deleteExpenseRecurringTransactionTemplateWithRelatedTransactionsTest() {

        // given
        Long userId = 3L;
        Long recurringTransactionTemplateId = 5L;
        RemovalRange range = RemovalRange.ALL;

        // when
        recTransactionService.delete(recurringTransactionTemplateId, range, userId);
        em.flush();
        em.clear();

        // then
        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recurringTransactionTemplateId);
        assertThat(recTransaction).isNull();

        List<Long> deletedTransactionIds = List.of(9L, 10L);
        List<Transaction> deletedTransactions = em.createQuery("""
                SELECT t FROM Transaction t
                WHERE t.id IN :deletedTransactionIds
                """, Transaction.class)
                .setParameter("deletedTransactionIds", deletedTransactionIds)
                .getResultList();

        assertThat(deletedTransactions.size()).isEqualTo(0);

        Account account = em.find(Account.class, 6L);
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("28600.00"));
        assertThat(account.getTotalExpense()).isEqualTo(new BigDecimal("21400.00"));
    }

    @Test
    @Order(13)
    public void deleteIncomeRecurringTransactionTemplateWithRelatedTransactionsTest() {

        // given
        Long userId = 5L;
        Long recurringTransactionTemplateId = 9L;
        RemovalRange range = RemovalRange.ALL;

        // when
        recTransactionService.delete(recurringTransactionTemplateId, range, userId);
        em.flush();
        em.clear();

        // then
        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recurringTransactionTemplateId);
        assertThat(recTransaction).isNull();

        List<Long> deletedTransactionIds = List.of(17L, 18L);
        List<Transaction> deletedTransactions = em.createQuery("""
                SELECT t FROM Transaction t
                WHERE t.id IN :deletedTransactionIds
                """, Transaction.class)
                .setParameter("deletedTransactionIds", deletedTransactionIds)
                .getResultList();

        assertThat(deletedTransactions.size()).isEqualTo(0);

        Account account = em.find(Account.class, 12L);
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("-1100.00"));
        assertThat(account.getTotalIncome()).isEqualTo(new BigDecimal("-100.00"));
    }

    @Test
    @Order(14)
    public void deleteRecurringTransactionThatDoesNotBelongToUserTest() {

        // given
        Long userId = 1L;
        Long recurringTransactionTemplateId = 10L;
        RemovalRange range = RemovalRange.TEMPLATE;

        // when and then
        assertThrows( NotFoundException.class, () -> {
            recTransactionService.delete(recurringTransactionTemplateId, range, userId);
        });
    }

    @Test
    @Order(15)
    public void updateRecurringTransactionTemplateOnlyTest() {

        // given
        Long userId = 1L;
        Long recTransactionId = 1L;
        UpdateRange range = UpdateRange.FUTURE;

        String changedDescription = "Abonament Netflix, od nowego miesiąca drożej";
        BigDecimal changedAmount = new BigDecimal("80.00");

        RecurringTransactionUpdateRequest updateReq =
                new RecurringTransactionUpdateRequest("Netflix", changedAmount, changedDescription);

        // when
        recTransactionService.update(recTransactionId, updateReq, range, userId);
        em.flush();
        em.clear();

        // then
        RecurringTransaction updatedRecurringTemplate = em.find(RecurringTransaction.class, recTransactionId);
        assertThat(updatedRecurringTemplate).isNotNull();
        assertThat(updatedRecurringTemplate.getAmount()).isEqualTo(changedAmount);
        assertThat(updatedRecurringTemplate.getDescription()).isEqualTo(changedDescription);
    }

    @Test
    @Order(16)
    public void updateRecurringTransactionTemplateWithAllRelatedTransactionsTest() {

        // given
        Long userId = 1L;
        Long recTransactionId = 1L;
        UpdateRange range = UpdateRange.ALL;

        String changedDescription = "Abonament Netflix, od nowego miesiąca drożej";
        BigDecimal changedAmount = new BigDecimal("80.00");

        RecurringTransactionUpdateRequest updateReq =
                new RecurringTransactionUpdateRequest("Netflix", changedAmount, changedDescription);

        // when
        recTransactionService.update(recTransactionId, updateReq, range, userId);
        em.flush();
        em.clear();


        // then
        RecurringTransaction updatedRecurringTemplate = em.find(RecurringTransaction.class, recTransactionId);
        assertThat(updatedRecurringTemplate).isNotNull();
        assertThat(updatedRecurringTemplate.getAmount()).isEqualTo(changedAmount);
        assertThat(updatedRecurringTemplate.getDescription()).isEqualTo(changedDescription);

        List<Long> updatedTransactionsIds = List.of(1L, 2L);
        List<Transaction> updatedTransactions = em.createQuery("""
                SELECT t FROM Transaction t
                WHERE t.id IN :updatedTransactionsIds
                """, Transaction.class)
                .setParameter("updatedTransactionsIds", updatedTransactionsIds)
                .getResultList();

        assertThat(updatedTransactions).isNotNull();

        updatedTransactions
                .forEach( updTransaction -> {
                    assertThat(updTransaction.getAmount()).isEqualTo(changedAmount);
                    assertThat(updTransaction.getDescription()).isEqualTo(changedDescription);
                });

        Account account = updatedRecurringTemplate.getAccount();
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1460.00"));
        assertThat(account.getTotalExpense()).isEqualTo(new BigDecimal("3540.00"));
    }

    @Test
    @Order(17)
    public void updateRecurringTransactionTemplateThatBelongToOtherUserTest() {

        // given
        Long userId = 1L;
        Long recTransactionId = 3L;
        UpdateRange range = UpdateRange.FUTURE;

        String changedDescription = "Abonament Netflix, od nowego miesiąca drożej";
        BigDecimal changedAmount = new BigDecimal("80.00");

        RecurringTransactionUpdateRequest updateReq =
                new RecurringTransactionUpdateRequest("Netflix", changedAmount, changedDescription);

        // when and then
        assertThrows(NotFoundException.class, () -> {
            recTransactionService.update(recTransactionId, updateReq, range, userId);
        });
    }

    @Test
    @Order(18)
    public void generateRecurringTransactionsTest() {

        // when
        recTransactionService.generateRecurringTransactions();
        em.flush();
        em.clear();

        List<Long> recTransactionIds = List.of(11L, 16L);

        List<RecurringTransaction> recurringTransaction = em.createQuery("""
                SELECT r FROM RecurringTransaction r
                WHERE r.id IN :recTransactionIds
                ORDER BY r.id
                """, RecurringTransaction.class)
                .setParameter("recTransactionIds", recTransactionIds)
                .getResultList();

        // then
        assertThat(recurringTransaction).isNotNull();
        assertThat(recurringTransaction.size()).isEqualTo(2);

        List<Transaction> createdTransactions = recurringTransaction.stream()
                        .flatMap( rt -> rt.getTransactions().stream())
                        .sorted(Comparator.comparing(Transaction::getId).reversed())
                        .limit(2)
                        .toList();

        assertThat(createdTransactions).isNotNull();
        assertThat(createdTransactions.size()).isEqualTo(2);
    }

    @Test
    @Order(19)
    public void changeRecurringTransactionTemplateStatusToInactiveTest() {

        // given
        Long userId = 3L;
        Long recTransactionTemplateId = 5L;
        Boolean isActive = false;

        // when
        recTransactionService.changeStatus(recTransactionTemplateId, isActive, userId);
        em.flush();
        em.clear();

        // then
        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recTransactionTemplateId);
        assertThat(recTransaction).isNotNull();
        assertThat(recTransaction.isActive()).isEqualTo(isActive);
    }

    @Test
    @Order(20)
    public void changeRecurringTransactionTemplateStatusToTheSameStatusItAlreadyIsTest() {

        // given
        Long userId = 3L;
        Long recTransactionTemplateId = 6L;
        Boolean isActive = false;

        // when and then
        assertThrows(StatusAlreadySetException.class, () -> {
           recTransactionService.changeStatus(recTransactionTemplateId, isActive, userId);
        });
    }

    @Test
    @Order(21)
    public void changeRecurringTransactionTemplateThatBelongToTheOtherUserTest() {

        // given
        Long userId = 3L;
        Long recTransactionTemplateId = 9L;
        Boolean isActive = false;

        // when and then
        assertThrows(NotFoundException.class, () -> {
            recTransactionService.changeStatus(recTransactionTemplateId, isActive, userId);
        });
    }

    @Test
    @Order(22)
    public void changeRecurringTransactionTemplateStatusToActiveWhereNextOccurrenceIsTodayTest() {

        // given
        Long userId = 5L;
        Long recTransactionTemplateId = 12L;
        Boolean isActive = true;

        // when
        recTransactionService.changeStatus(recTransactionTemplateId, isActive, userId);
        em.flush();
        em.clear();

        // then
        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recTransactionTemplateId);
        assertThat(recTransaction).isNotNull();
        assertThat(recTransaction.isActive()).isEqualTo(isActive);

    }

    @Test
    @Order(24)
    public void changeRecurringTransactionTemplateStatusToActiveWhereNextOccurrenceFromBeforeToAfterCurrentDayTest() {

        // given
        Long userId = 5L;
        Long recTransactionTemplateId = 13L;
        Boolean isActive = true;

        // when
        recTransactionService.changeStatus(recTransactionTemplateId, isActive, userId);

        // then
        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recTransactionTemplateId);
        assertThat(recTransaction).isNotNull();
        assertThat(recTransaction.isActive()).isEqualTo(isActive);
    }

    @Test
    @Order(25)
    public void changeRecurringTransactionTemplateToActiveWhereNextOccurrenceFromBeforeToCurrentDayTest() {

        // given
        Long userId = 5L;
        Long recTransactionTemplateId = 14L;
        Boolean isActive = true;

        // when
        recTransactionService.changeStatus(recTransactionTemplateId, isActive, userId);

        // then
        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recTransactionTemplateId);
        assertThat(recTransaction).isNotNull();
        assertThat(recTransaction.isActive()).isEqualTo(isActive);

    }

    @Test
    @Order(27)
    public void getUpcomingTransactionsPagedResponseForAccountThatDoNotBelongToUserTest() {

        // given
        Long userId = 1L;
        List<Long> accountIds = List.of(1L, 2L, 3L, 4L);

        UpcomingTransactionFilterParams filterParams =
                new UpcomingTransactionFilterParams();
        filterParams.setAccountIds(accountIds);

        // when and then
        assertThrows(NotFoundException.class, () -> {
                this.recTransactionService.getUpcomingTransactionsPage(
                        new PaginationParams(),
                        filterParams,
                        userId
                );
        });
    }

    @Test
    @Order(27)
    public void getUpcomingTransactionsPagedResponseTest() {

        // given
        Long userId = 1L;
        PaginationParams paginationParams =
                new PaginationParams();
        paginationParams.setLimit(DEFAULT_PAGE_LIMIT-1);
        UpcomingTransactionFilterParams filterParams =
                new UpcomingTransactionFilterParams();
        filterParams.setRange(UpcomingTransactionsTimeRange.NEXT_MONTH);

        // when
        PagedResponse<UpcomingTransactionSummary> response =
                this.recTransactionService.getUpcomingTransactionsPage(
                    paginationParams,
                    filterParams,
                    userId
                );

        // then
        assertThat(response).isNotNull();
        assertThat(response.data()).isNotNull();
        assertThat(response.data().size()).isNotZero();
        assertThat(response.pagination()).isNotNull();
        assertThat(response.links()).isNull();
    }
}
