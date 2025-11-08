package com.example.budget_management_app.recurring_transaction.integration;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.StatusAlreadySetException;
import com.example.budget_management_app.common.exception.TransactionTypeMismatchException;
import com.example.budget_management_app.recurring_transaction.domain.*;
import com.example.budget_management_app.recurring_transaction.dto.*;
import com.example.budget_management_app.recurring_transaction.service.RecurringTransactionService;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.transaction_common.dto.PageRequest;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RecurringTransactionService Integration Tests")
public class ServiceTest {

    @Autowired
    private RecurringTransactionService recTransactionService;

    @Autowired
    private EntityManager em;

    @Test
    @Order(1)
    public void getRecurringTransactionsTemplatePagedResponseForUserTest() {

        long userId = 1L;
        int page = 1;
        int limit = 3;
        int expectedNumberOfElements = 3;

        PagedResponse<RecurringTransactionSummary> pagedResponse
                = recTransactionService.getSummaries(userId, page, limit);

        assertThat(pagedResponse).isNotNull();
        assertThat(pagedResponse.data()).isNotNull();
        assertThat(pagedResponse.pagination()).isNotNull();

        assertThat(pagedResponse.data().size()).isEqualTo(expectedNumberOfElements);

        System.out.println(pagedResponse);
    }

    @Test
    @Order(2)
    public void getRecurringTransactionsTemplatePagedResponseForSecondUserTest() {

        long userId = 5L;
        int page = 1;
        int limit = 3;
        int expectedNumberOfElements = 3;

        PagedResponse<RecurringTransactionSummary> pagedResponse =
                recTransactionService.getSummaries(userId, page, limit);

        assertThat(pagedResponse).isNotNull();
        assertThat(pagedResponse.data()).isNotNull();
        assertThat(pagedResponse.pagination()).isNotNull();

        assertThat(pagedResponse.data().size()).isEqualTo(expectedNumberOfElements);

        System.out.println(pagedResponse);
    }

    @Test
    @Order(3)
    public void getRecurringTransactionTemplateForUserDetailsTest() {

        long userId = 1L;
        long recTransactionId = 1L;

        RecurringTransactionDetailsResponse response =
                recTransactionService.getDetails(recTransactionId, userId);

        assertThat(response).isNotNull();
        System.out.println(response);
    }

    @Test
    @Order(4)
    public void getRecurringTransactionTemplateByUserItDoesNotBelongTest() {

        long userId = 1L;
        long recTransactionId = 3L;

        assertThrows( NotFoundException.class, () -> {
            recTransactionService.getDetails(recTransactionId, userId);
        });
    }

    @Test
    @Order(5)
    public void getRecurringTransactionTemplateThatDoesNotExistTest() {

        long userId = 1L;
        long recTransactionId = 15L;

        assertThrows( NotFoundException.class, () -> {
            recTransactionService.getDetails(recTransactionId, userId);
        });
    }

    @Test
    @Order(6)
    public void createRecurringTransactionTemplateWhichStartsAfterCurrentDayTest() {

        long userId = 1L;
        long accountId = 1L;
        long categoryId = 4L;

        RecurringTransactionCreateRequest createReq =
                new RecurringTransactionCreateRequest(
                    BigDecimal.valueOf(250L), "Orange", TransactionType.EXPENSE,
                    "Rachunek za telefon", LocalDate.now().plusDays(2), null,
                        RecurringInterval.MONTH, 1, accountId, categoryId
                );

        RecurringTransactionCreateResponse createResponse =
                recTransactionService.create(userId, createReq);

        em.clear();

        assertThat(createResponse).isNotNull();
        assertThat(createResponse.newTransaction()).isEqualTo(false);
        assertThat(createResponse.newTransactionView()).isNull();

        System.out.println(createResponse);

        RecurringTransaction recurringTransaction = em.find(RecurringTransaction.class, createResponse.id());
        assertThat(createResponse.id()).isEqualTo(recurringTransaction.getId());
        assertThat(recurringTransaction.getCategory().getId()).isEqualTo(categoryId);
        assertThat(recurringTransaction.getAccount().getId()).isEqualTo(accountId);

        System.out.println(recurringTransaction);
    }

    @Test
    @Order(7)
    public void createRecurringTransactionTemplateWhichStartsAtTheCurrentDayAfterNoonTest() {

        long userId = 2;
        long accountId = 4L;
        long categoryId = 6L;

        RecurringTransactionCreateRequest createReq =
                new RecurringTransactionCreateRequest(
                        BigDecimal.valueOf(1250L), "Czynsz", TransactionType.EXPENSE,
                        "Miesięczna opłata za czynsz", LocalDate.now(), null,
                        RecurringInterval.MONTH, 1, accountId, categoryId
                );

        RecurringTransactionCreateResponse createResponse =
                recTransactionService.create(userId, createReq);

        em.clear();

        assertThat(createResponse).isNotNull();
        assertThat(createResponse.newTransaction()).isEqualTo(true);
        assertThat(createResponse.newTransactionView()).isNotNull();

        System.out.println(createResponse);

        RecurringTransaction recurringTransaction = em.find(RecurringTransaction.class, createResponse.id());
        assertThat(createResponse.id()).isEqualTo(recurringTransaction.getId());
        assertThat(recurringTransaction.getCategory().getId()).isEqualTo(categoryId);
        assertThat(recurringTransaction.getAccount().getId()).isEqualTo(accountId);

        System.out.println(recurringTransaction);

        Optional<Transaction> optCreatedTransaction = recurringTransaction
                .getTransactions()
                .stream()
                .findFirst();

        assertThat(optCreatedTransaction).isPresent();
        Transaction createdTransaction = optCreatedTransaction.get();
        assertThat(createdTransaction.getAccount().getId()).isEqualTo(accountId);
        assertThat(createdTransaction.getCategory().getId()).isEqualTo(categoryId);

        System.out.println(createdTransaction);

        Account account = createdTransaction.getAccount();
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1750.00"));
        assertThat(account.getTotalExpense()).isEqualTo(new BigDecimal("2949.50"));

        System.out.println(account);
    }

    @Test
    @Order(8)
    public void createRecurringTransactionTemplateWhichStartsAtTheCurrentDayBeforeNoonTest() {

        long userId = 3L;
        long accountId = 6L;
        long categoryId = 12L;

        RecurringTransactionCreateRequest createReq =
                new RecurringTransactionCreateRequest(
                        BigDecimal.valueOf(250L), "Karnet na siłownie", TransactionType.EXPENSE,
                        "Miesięczny karnet na siłownie", LocalDate.now(), null,
                        RecurringInterval.MONTH, 1, accountId, categoryId
                );

        RecurringTransactionCreateResponse createResponse =
                recTransactionService.create(userId, createReq);

        em.clear();

        assertThat(createResponse).isNotNull();
        assertThat(createResponse.newTransaction()).isEqualTo(false);
        assertThat(createResponse.newTransactionView()).isNull();

        System.out.println(createResponse);

        RecurringTransaction recurringTransaction = em.find(RecurringTransaction.class, createResponse.id());
        assertThat(createResponse.id()).isEqualTo(recurringTransaction.getId());
        assertThat(recurringTransaction.getCategory().getId()).isEqualTo(categoryId);
        assertThat(recurringTransaction.getAccount().getId()).isEqualTo(accountId);

        System.out.println(recurringTransaction);
    }

    @Test
    @Order(9)
    public void createRecurringTransactionTemplateForOtherUserTest() {

        long userId = 1L;
        long accountId = 4L;
        long categoryId = 6L;

        RecurringTransactionCreateRequest createReq =
                new RecurringTransactionCreateRequest(
                        BigDecimal.valueOf(1250L), "Czynsz", TransactionType.EXPENSE,
                        "Miesięczna opłata za czynsz", LocalDate.now(), null,
                        RecurringInterval.MONTH, 1, accountId, categoryId
                );

        assertThrows(NotFoundException.class, () -> {
            recTransactionService.create(userId, createReq);
        });
    }

    @Test
    @Order(10)
    public void createRecurringTransactionWithCategoryWithIncompatibleTypeTest() {

        long userId = 2;
        long accountId = 4L;
        long categoryId = 6L;

        RecurringTransactionCreateRequest createReq =
                new RecurringTransactionCreateRequest(
                        BigDecimal.valueOf(400), "Premia", TransactionType.INCOME,
                        null, LocalDate.now(), null,
                        RecurringInterval.MONTH, 1, accountId, categoryId
                );

        assertThrows(TransactionTypeMismatchException.class, () -> {
            recTransactionService.create(userId, createReq);
        });
    }

    @Test
    @Order(11)
    public void deleteRecurringTransactionTemplateWithoutTransactionsTest() {

        long userId = 2L;
        long recurringTransactionTemplateId = 3L;
        RemovalRange range = RemovalRange.TEMPLATE;

        recTransactionService.delete(recurringTransactionTemplateId, userId, range);

        em.flush();
        em.clear();

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

        detachedTransactions.forEach(System.out::println);
    }

    @Test
    @Order(12)
    public void deleteExpenseRecurringTransactionTemplateWithRelatedTransactionsTest() {

        long userId = 3L;
        long recurringTransactionTemplateId = 5L;
        RemovalRange range = RemovalRange.ALL;

        recTransactionService.delete(recurringTransactionTemplateId, userId, range);

        em.flush();
        em.clear();

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

        System.out.println(account);
    }

    @Test
    @Order(13)
    public void deleteIncomeRecurringTransactionTemplateWithRelatedTransactionsTest() {

        long userId = 5L;
        long recurringTransactionTemplateId = 9L;
        RemovalRange range = RemovalRange.ALL;

        recTransactionService.delete(recurringTransactionTemplateId, userId, range);

        em.flush();
        em.clear();

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

        System.out.println(account);
    }

    @Test
    @Order(14)
    public void deleteRecurringTransactionThatDoesNotBelongToUserTest() {

        long userId = 1L;
        long recurringTransactionTemplateId = 10L;
        RemovalRange range = RemovalRange.TEMPLATE;

        assertThrows( NotFoundException.class, () -> {
            recTransactionService.delete(recurringTransactionTemplateId, userId, range);
        });
    }

    @Test
    @Order(15)
    public void updateRecurringTransactionTemplateOnlyTest() {

        long userId = 1L;
        long recTransactionId = 1L;
        UpdateRange range = UpdateRange.FUTURE;

        String changedDescription = "Abonament Netflix, od nowego miesiąca drożej";
        BigDecimal changedAmount = new BigDecimal("80.00");

        RecurringTransactionUpdateRequest updateReq =
                new RecurringTransactionUpdateRequest(changedAmount, "Netflix", changedDescription);

        recTransactionService.update(recTransactionId, userId, updateReq, range);

        em.flush();
        em.clear();

        RecurringTransaction updatedRecurringTemplate = em.find(RecurringTransaction.class, recTransactionId);
        assertThat(updatedRecurringTemplate).isNotNull();
        assertThat(updatedRecurringTemplate.getAmount()).isEqualTo(changedAmount);
        assertThat(updatedRecurringTemplate.getDescription()).isEqualTo(changedDescription);

        System.out.println(updatedRecurringTemplate);
    }

    @Test
    @Order(16)
    public void updateRecurringTransactionTemplateWithAllRelatedTransactionsTest() {

        long userId = 1L;
        long recTransactionId = 1L;
        UpdateRange range = UpdateRange.ALL;

        String changedDescription = "Abonament Netflix, od nowego miesiąca drożej";
        BigDecimal changedAmount = new BigDecimal("80.00");

        RecurringTransactionUpdateRequest updateReq =
                new RecurringTransactionUpdateRequest(changedAmount, "Netflix", changedDescription);

        recTransactionService.update(recTransactionId, userId, updateReq, range);

        em.flush();
        em.clear();

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

        updatedTransactions.forEach(System.out::println);

        Account account = updatedRecurringTemplate.getAccount();
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1460.00"));
        assertThat(account.getTotalExpense()).isEqualTo(new BigDecimal("3540.00"));
        System.out.println(account);
    }

    @Test
    @Order(17)
    public void updateRecurringTransactionTemplateThatBelongToOtherUserTest() {

        long userId = 1L;
        long recTransactionId = 3L;
        UpdateRange range = UpdateRange.FUTURE;

        String changedDescription = "Abonament Netflix, od nowego miesiąca drożej";
        BigDecimal changedAmount = new BigDecimal("80.00");

        RecurringTransactionUpdateRequest updateReq =
                new RecurringTransactionUpdateRequest(changedAmount, "Netflix", changedDescription);

        assertThrows(NotFoundException.class, () -> {
            recTransactionService.update(recTransactionId, userId, updateReq, range);
        });
    }

    @Test
    @Order(18)
    public void generateRecurringTransactionsTest() {

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

        assertThat(recurringTransaction).isNotNull();
        assertThat(recurringTransaction.size()).isEqualTo(2);

        recurringTransaction.forEach(System.out::println);

        List<Transaction> createdTransactions = recurringTransaction.stream()
                        .flatMap( rt -> rt.getTransactions().stream())
                        .sorted(Comparator.comparing(Transaction::getId).reversed())
                        .limit(2)
                        .toList();

        assertThat(createdTransactions).isNotNull();
        assertThat(createdTransactions.size()).isEqualTo(2);

        createdTransactions.forEach(transaction -> {
            System.out.println("Transaction: " + transaction + "\n"
                    + " Account: " + transaction.getAccount());
        });
    }

    @Test
    @Order(19)
    public void changeRecurringTransactionTemplateStatusToInactiveTest() {

        long userId = 3L;
        long recTransactionTemplateId = 5L;
        boolean isActive = false;

        recTransactionService.changeStatus(recTransactionTemplateId, userId, isActive);

        em.flush();
        em.clear();

        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recTransactionTemplateId);
        assertThat(recTransaction).isNotNull();
        assertThat(recTransaction.isActive()).isEqualTo(isActive);
        System.out.println(recTransaction);
    }

    @Test
    @Order(20)
    public void changeRecurringTransactionTemplateStatusToTheSameStatusItAlreadyIsTest() {

        long userId = 3L;
        long recTransactionTemplateId = 6L;
        boolean isActive = false;

        assertThrows(StatusAlreadySetException.class, () -> {
           recTransactionService.changeStatus(recTransactionTemplateId, userId, isActive);
        });
    }

    @Test
    @Order(21)
    public void changeRecurringTransactionTemplateThatBelongToTheOtherUserTest() {

        long userId = 3L;
        long recTransactionTemplateId = 9L;
        boolean isActive = false;

        assertThrows(NotFoundException.class, () -> {
            recTransactionService.changeStatus(recTransactionTemplateId, userId, isActive);
        });
    }

    @Test
    @Order(22)
    public void changeRecurringTransactionTemplateStatusToActiveWhereNextOccurrenceIsTodayAfterNoonTest() {

        long userId = 5L;
        long recTransactionTemplateId = 12L;
        boolean isActive = true;

        recTransactionService.changeStatus(recTransactionTemplateId, userId, isActive);

        em.flush();
        em.clear();

        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recTransactionTemplateId);
        assertThat(recTransaction).isNotNull();
        assertThat(recTransaction.isActive()).isEqualTo(isActive);
        System.out.println(recTransaction);

    }

    @Test
    @Order(23)
    public void changeRecurringTransactionTemplateStatusToActiveWhereNextOccurrenceIsTodayBeforeNoonTest() {

        long userId = 5L;
        long recTransactionTemplateId = 12L;
        boolean isActive = true;

        recTransactionService.changeStatus(recTransactionTemplateId, userId, isActive);

        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recTransactionTemplateId);
        assertThat(recTransaction).isNotNull();
        assertThat(recTransaction.isActive()).isEqualTo(isActive);
        System.out.println(recTransaction);
    }

    @Test
    @Order(24)
    public void changeRecurringTransactionTemplateStatusToActiveWhereNextOccurrenceFromBeforeToAfterCurrentDayTest() {

        long userId = 5L;
        long recTransactionTemplateId = 13L;
        boolean isActive = true;

        recTransactionService.changeStatus(recTransactionTemplateId, userId, isActive);

        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recTransactionTemplateId);
        assertThat(recTransaction).isNotNull();
        assertThat(recTransaction.isActive()).isEqualTo(isActive);
        System.out.println(recTransaction);
    }

    @Test
    @Order(25)
    public void changeRecurringTransactionTemplateToActiveWhereNextOccurrenceFromBeforeToCurrentDayBeforeNoonTest() {

        long userId = 5L;
        long recTransactionTemplateId = 14L;
        boolean isActive = true;

        recTransactionService.changeStatus(recTransactionTemplateId, userId, isActive);

        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recTransactionTemplateId);
        assertThat(recTransaction).isNotNull();
        assertThat(recTransaction.isActive()).isEqualTo(isActive);
        System.out.println(recTransaction);

    }

    @Test
    @Order(26)
    public void changeRecurringTransactionTemplateToActiveWhereNextOccurrenceFromBeforeToCurrentDayAfterNoonTest() {

        long userId = 5L;
        long recTransactionTemplateId = 15L;
        boolean isActive = true;

        recTransactionService.changeStatus(recTransactionTemplateId, userId, isActive);

        RecurringTransaction recTransaction = em.find(RecurringTransaction.class, recTransactionTemplateId);
        assertThat(recTransaction).isNotNull();
        assertThat(recTransaction.isActive()).isEqualTo(isActive);
        System.out.println(recTransaction);
    }

    @Test
    @Order(27)
    public void getUpcomingTransactionsPagedResponseForAccountThatDoNotBelongToUserTest() {

        Long userId = 1L;
        List<Long> accountIds = List.of(1L, 2L, 3L, 4L);

        int page = 1;
        int limit = 4;
        PageRequest pageReq =
                new PageRequest(page, limit);
        UpcomingTransactionSearchCriteria searchCriteria =
                new UpcomingTransactionSearchCriteria(
                        UpcomingTransactionsTimeRange.NEXT_7_DAYS,
                        accountIds
                );

        assertThrows(NotFoundException.class, () -> {
                this.recTransactionService.getUpcommingTransactionsPage(
                        pageReq,
                        searchCriteria,
                        userId
                );
        });
    }

    @Test
    @Order(27)
    public void getUpcomingTransactionsPagedResponseTest() {

        Long userId = 1L;
        List<Long> accountIds = List.of(1L, 2L, 3L);

        int page = 1;
        int limit = 4;
        PageRequest pageReq =
                new PageRequest(page, limit);
        UpcomingTransactionSearchCriteria searchCriteria =
                new UpcomingTransactionSearchCriteria(
                        UpcomingTransactionsTimeRange.NEXT_MONTH,
                        accountIds
                );

        PagedResponse<UpcomingTransactionSummary> response =
                this.recTransactionService.getUpcommingTransactionsPage(
                    pageReq,
                    searchCriteria,
                    userId
                );

        assertThat(response).isNotNull();
        assertThat(response.data()).isNotNull();
        assertThat(response.data().size()).isNotZero();
        assertThat(response.pagination()).isNotNull();
        assertThat(response.links()).isNull();
        System.out.println(response);
    }
}
