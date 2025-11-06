package com.example.budget_management_app.transaction.integration;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.common.exception.CategoryChangeNotAllowedException;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.TransactionTypeMismatchException;
import com.example.budget_management_app.transaction.domain.*;
import com.example.budget_management_app.transaction.dto.*;
import com.example.budget_management_app.transaction.service.TransactionService;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TransactionService Integration Tests")
public class ServiceTests {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private EntityManager em;

    @Test
    @Order(1)
    public void getTransactionPageFirstPageWithDefaultFiltersTest() {

        long userId = 1L;
        int page = 1;
        int limit = 4;
        List<Long> userAccountsIds = List.of(1L, 2L, 3L);
        List<Long> userCategoriesIds = List.of(1L, 2L, 3L, 4L);
        PagedResponse<TransactionSummary> transactionsPage = transactionService.getViews(
                page,
                limit,
                TransactionTypeFilter.ALL,
                TransactionModeFilter.ALL,
                userAccountsIds,
                userCategoriesIds,
                LocalDate.of(2025,9,1),
                null,
                SortedBy.DATE,
                SortDirection.DESC,
                userId
        );

        assertThat(transactionsPage).isNotNull();
        assertThat(transactionsPage.pagination()).isNotNull();
        assertThat(transactionsPage.data()).isNotNull();
        assertThat(transactionsPage.data().size()).isEqualTo(limit);

        System.out.println(transactionsPage.data());
        System.out.println(transactionsPage.pagination());
    }

    @Test
    @Order(2)
    public void getTransactionPageLastPageWithCustomFiltersTest() {

        long userId = 1L;
        int page = 3;
        int limit = 4;
        List<Long> userAccountsIds = List.of(1L, 2L, 3L);
        List<Long> userCategoriesIds = List.of(1L, 2L, 3L, 4L);
        PagedResponse<TransactionSummary> transactionsPage = transactionService.getViews(
                page,
                limit,
                TransactionTypeFilter.ALL,
                TransactionModeFilter.ALL,
                userAccountsIds,
                userCategoriesIds,
                LocalDate.of(2025,10,1),
                null,
                SortedBy.DATE,
                SortDirection.DESC,
                userId
        );

        assertThat(transactionsPage).isNotNull();
        assertThat(transactionsPage.pagination()).isNotNull();
        assertThat(transactionsPage.data()).isNotNull();
        assertThat(transactionsPage.data().size()).isEqualTo(2);
        System.out.println(transactionsPage.data());
        System.out.println(transactionsPage.pagination());
    }

    @Test
    @Order(3)
    public void getTransactionPageForAccountsThatDoNotBelongToUserTest() {

        long userId = 1L;
        int page = 3;
        int limit = 4;
        List<Long> userAccountsIds = List.of(1L, 2L, 3L, 4L);
        List<Long> userCategoriesIds = List.of(1L, 2L, 3L, 4L);

        assertThrows(NotFoundException.class, () -> {
            transactionService.getViews(
                    page,
                    limit,
                    TransactionTypeFilter.ALL,
                    TransactionModeFilter.ALL,
                    userAccountsIds,
                    userCategoriesIds,
                    LocalDate.of(2025,10,1),
                    null,
                    SortedBy.DATE,
                    SortDirection.DESC,
                    userId
            );
        });
    }

    @Test
    @Order(4)
    public void getTransactionPageForCategoriesThatDoNotBelongToUserTest() {

        long userId = 1L;
        int page = 3;
        int limit = 4;
        List<Long> userAccountsIds = List.of(1L, 2L, 3L);
        List<Long> userCategoriesIds = List.of(1L, 2L, 3L, 4L, 5L);

        assertThrows(NotFoundException.class, () -> {
            transactionService.getViews(
                    page,
                    limit,
                    TransactionTypeFilter.ALL,
                    TransactionModeFilter.ALL,
                    userAccountsIds,
                    userCategoriesIds,
                    LocalDate.of(2025,10,1),
                    null,
                    SortedBy.DATE,
                    SortDirection.DESC,
                    userId
            );
        });
    }

    @Test
    @Order(5)
    public void createTransactionForCategoryThatDoesNotBelongToUserTest() {

        long userId = 1L;
        long categoryId = 5L;
        long accountId = 1L;

        TransactionCreateRequest createReq =
                new TransactionCreateRequest(
                        new BigDecimal("250"), "Książki", TransactionType.EXPENSE,
                        "Nowe książki do szkoły", accountId, categoryId
                );

        assertThrows(NotFoundException.class, () -> {
           transactionService.create(createReq, userId);
        });
    }

    @Test
    @Order(6)
    public void createTransactionForAccountThatDoesNotBelongToUserTest() {

        long userId = 1L;
        long categoryId = 1L;
        long accountId = 4L;

        TransactionCreateRequest createReq =
                new TransactionCreateRequest(
                        new BigDecimal("200"), "Obiad w pogodnym", TransactionType.EXPENSE,
                        null, accountId, categoryId
                );

        assertThrows(NotFoundException.class, () -> {
            transactionService.create(createReq, userId);
        });
    }

    @Test
    @Order(7)
    @Transactional
    public void createExpenseTypeTransactionTest() {

        long userId = 2L;
        long categoryId = 6L;
        long accountId = 4L;

        TransactionCreateRequest createReq =
                new TransactionCreateRequest(
                        new BigDecimal("800.00"), "Nowy materac", TransactionType.EXPENSE,
                        null, accountId, categoryId
                );

        TransactionCreateResponse response = transactionService.create(createReq, userId);

        System.out.println(response);

        em.clear();

        Transaction createdTransaction = em.find(Transaction.class, response.id());
        assertThat(createdTransaction).isNotNull();
        assertThat(createdTransaction.getId()).isEqualTo(response.id());

        Account assignedAccount = createdTransaction.getAccount();
        assertThat(assignedAccount).isNotNull();
        assertThat(assignedAccount.getId()).isEqualTo(accountId);
        assertThat(assignedAccount.getBalance()).isEqualTo(new BigDecimal("2200.00"));
        assertThat(assignedAccount.getTotalExpense()).isEqualTo(new BigDecimal("2499.50"));

        assertThat(createdTransaction.getCategory().getId()).isEqualTo(categoryId);

        System.out.println(createdTransaction);
        System.out.println(assignedAccount);
    }

    @Test
    @Order(8)
    @Transactional
    public void createIncomeTypeTransactionTest() {

        long userId = 1L;
        long categoryId = 3L;
        long accountId = 1L;

        TransactionCreateRequest createReq =
                new TransactionCreateRequest(
                        new BigDecimal("800.00"), "Premia", TransactionType.INCOME,
                        null, accountId, categoryId
                );

        TransactionCreateResponse response = transactionService.create(createReq, userId);

        System.out.println(response);

        em.clear();

        Transaction createdTransaction = em.find(Transaction.class, response.id());
        assertThat(createdTransaction).isNotNull();
        assertThat(createdTransaction.getId()).isEqualTo(response.id());

        Account assignedAccount = createdTransaction.getAccount();
        assertThat(assignedAccount).isNotNull();
        assertThat(assignedAccount.getId()).isEqualTo(accountId);
        assertThat(assignedAccount.getBalance()).isEqualTo(new BigDecimal("2300.00"));
        assertThat(assignedAccount.getTotalIncome()).isEqualTo(new BigDecimal("5800.00"));

        assertThat(createdTransaction.getCategory().getId()).isEqualTo(categoryId);

        System.out.println(createdTransaction);
        System.out.println(assignedAccount);
    }

    @Test
    @Order(9)
    public void createTransactionWithCategoryWithIncompatibleTypeAssignedTest() {

        long userId = 2L;
        long categoryId = 6L;
        long accountId = 4L;

        TransactionCreateRequest createReq =
                new TransactionCreateRequest(
                        new BigDecimal("200"), "Urodziny", TransactionType.INCOME,
                        null, accountId, categoryId
                );

        assertThrows(TransactionTypeMismatchException.class, () -> {
            transactionService.create(createReq, userId);
        });
    }

    @Test
    @Order(10)
    public void changeRecurringTransactionCategoryTest() {

        long userId = 3L;
        long transactionId = 9L;
        long currentTransactionCategoryId = 8L;
        long newTransactionCategoryId = 10L;
        long accountId = 0L;

        TransactionCategoryUpdateRequest updReq =
                new TransactionCategoryUpdateRequest(
                        currentTransactionCategoryId,
                        newTransactionCategoryId,
                        accountId
                );

        assertThrows( CategoryChangeNotAllowedException.class, () -> {
           transactionService.changeCategory(transactionId, userId, updReq);
        });
    }

    @Test
    @Order(11)
    public void changeCategoryForTransactionThatBelongToOtherUserTest() {

        long userId = 1L;
        long transactionId = 30L;
        long currentTransactionCategoryId = 6L;
        long newTransactionCategoryId = 5L;
        long accountId = 0L;

        TransactionCategoryUpdateRequest updReq =
                new TransactionCategoryUpdateRequest(
                        currentTransactionCategoryId,
                        newTransactionCategoryId,
                        accountId
                );

        assertThrows( NotFoundException.class, () -> {
            transactionService.changeCategory(transactionId, userId, updReq);
        });
    }

    @Test
    @Order(12)
    public void changeTransactionCategoryButTransactionDoesNotBelongToProvidedCategoryTest() {

        long userId = 1L;
        long transactionId = 22L;    // transaction with this ID does not belong to the category with the ID given below
        long currentTransactionCategoryId = 6L;
        long newTransactionCategoryId = 1L;
        long accountId = 0L;

        TransactionCategoryUpdateRequest updReq =
                new TransactionCategoryUpdateRequest(
                        currentTransactionCategoryId,
                        newTransactionCategoryId,
                        accountId
                );

        assertThrows( NotFoundException.class, () -> {
            transactionService.changeCategory(transactionId, userId, updReq);
        });
    }

    @Test
    @Order(13)
    public void changeTransactionCategoryWhereNewCategoryDoesNotBelongToTheUserTest() {

        long userId = 1L;
        long transactionId = 22L;
        long currentTransactionCategoryId = 2L;
        long newTransactionCategoryId = 6L;
        long accountId = 0L;

        TransactionCategoryUpdateRequest updReq =
                new TransactionCategoryUpdateRequest(
                        currentTransactionCategoryId,
                        newTransactionCategoryId,
                        accountId
                );

        assertThrows( NotFoundException.class, () -> {
            transactionService.changeCategory(transactionId, userId, updReq);
        });
    }

    @Test
    @Order(14)
    @Transactional
    public void changeTransactionCategoryTest() {

        long userId = 1L;
        long transactionId = 22L;
        long currentTransactionCategoryId = 2L;
        long newTransactionCategoryId = 4L;
        long accountId = 0L;

        TransactionCategoryUpdateRequest updReq =
                new TransactionCategoryUpdateRequest(
                        currentTransactionCategoryId,
                        newTransactionCategoryId,
                        accountId
                );

        TransactionCategoryUpdateResponse updateResponse =
                transactionService.changeCategory(
                        transactionId, userId, updReq
                );

        System.out.println(updateResponse);

        em.flush();
        em.clear();

        assertThat(updateResponse).isNotNull();
        assertThat(updateResponse.categoryId()).isEqualTo(newTransactionCategoryId);

        Transaction transaction = em.find(Transaction.class, transactionId);
        assertThat(transaction).isNotNull();

        assertThat(transaction.getCategory().getId()).isEqualTo(newTransactionCategoryId);

        System.out.println(transaction);
        System.out.println(transaction.getCategory());
    }

    @Test
    @Order(15)
    public void changeTransactionCategoryWhereNewCategoryIsIncompatibleType() {

        long userId = 1L;
        long transactionId = 22L;
        long currentTransactionCategoryId = 2L;
        long newTransactionCategoryId = 3L;
        long accountId = 0L;

        TransactionCategoryUpdateRequest updReq =
                new TransactionCategoryUpdateRequest(
                        currentTransactionCategoryId,
                        newTransactionCategoryId,
                        accountId
                );


        assertThrows(TransactionTypeMismatchException.class, () -> {
            transactionService.changeCategory(
                    transactionId, userId, updReq
            );
        });

    }

    @Test
    @Order(16)
    public void updateTransactionThatBelongToOtherUserTest() {

        long userId = 2L;
        long transactionId = 22L;

        TransactionUpdateRequest updateReq =
                new TransactionUpdateRequest(
                        "Bilet miesięczny", new BigDecimal("70"),
                        "Bilet miesięczny podrożał"
                );

        assertThrows(NotFoundException.class, () -> {
            transactionService.update(transactionId, userId, updateReq);
        });
    }

    @Test
    @Order(17)
    @Transactional
    public void updateTransactionIncreasingTransactionExpenseValueTest() {

        long userId = 1L;
        long transactionId = 22L;

        TransactionUpdateRequest updateReq =
                new TransactionUpdateRequest(
                        "Bilet miesięczny", new BigDecimal("70"),
                        "Bilet miesięczny podrożał"
                );

        transactionService.update(transactionId, userId, updateReq);

        em.flush();
        em.clear();

        Transaction updatedTransaction = em.find(Transaction.class, transactionId);
        assertThat(updatedTransaction).isNotNull();
        assertThat(updatedTransaction.getId()).isEqualTo(transactionId);

        System.out.println(updatedTransaction);

        Account account = updatedTransaction.getAccount();
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1480.00"));
        assertThat(account.getTotalExpense()).isEqualTo(new BigDecimal("3520.00"));

        System.out.println(account);
    }

    @Test
    @Order(18)
    @Transactional
    public void updateTransactionDecreasingTransactionExpenseValueTest() {

        long userId = 1L;
        long transactionId = 22L;

        TransactionUpdateRequest updateReq =
                new TransactionUpdateRequest(
                        "Bilet miesięczny", new BigDecimal("40"),
                        "Bilet miesięczny potaniał"
                );

        transactionService.update(transactionId, userId, updateReq);

        em.flush();
        em.clear();

        Transaction updatedTransaction = em.find(Transaction.class, transactionId);
        assertThat(updatedTransaction).isNotNull();
        assertThat(updatedTransaction.getId()).isEqualTo(transactionId);

        System.out.println(updatedTransaction);

        Account account = updatedTransaction.getAccount();
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1510.00"));
        assertThat(account.getTotalExpense()).isEqualTo(new BigDecimal("3490.00"));

        System.out.println(account);
    }

    @Test
    @Order(19)
    @Transactional
    public void updateTransactionIncreasingTransactionIncomeValueTest() {

        long userId = 1L;
        long transactionId = 23L;

        TransactionUpdateRequest updateReq =
                new TransactionUpdateRequest(
                        "Wypłata", new BigDecimal("5100"),
                        "Wypłata z premią - 100zł"
                );

        transactionService.update(transactionId, userId, updateReq);

        em.flush();
        em.clear();

        Transaction updatedTransaction = em.find(Transaction.class, transactionId);
        assertThat(updatedTransaction).isNotNull();
        assertThat(updatedTransaction.getId()).isEqualTo(transactionId);

        System.out.println(updatedTransaction);

        Account account = updatedTransaction.getAccount();
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1600.00"));
        assertThat(account.getTotalIncome()).isEqualTo(new BigDecimal("5100.00"));

        System.out.println(account);
    }

    @Test
    @Order(20)
    @Transactional
    public void updateTransactionDecreasingTransactionIncomeValueTest() {

        long userId = 1L;
        long transactionId = 23L;

        TransactionUpdateRequest updateReq =
                new TransactionUpdateRequest(
                        "Wypłata", new BigDecimal("4900"),
                        "Wypłata obcięta o 100zł"
                );

        transactionService.update(transactionId, userId, updateReq);

        em.flush();
        em.clear();

        Transaction updatedTransaction = em.find(Transaction.class, transactionId);
        assertThat(updatedTransaction).isNotNull();
        assertThat(updatedTransaction.getId()).isEqualTo(transactionId);

        System.out.println(updatedTransaction);

        Account account = updatedTransaction.getAccount();
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1400.00"));
        assertThat(account.getTotalIncome()).isEqualTo(new BigDecimal("4900.00"));

        System.out.println(account);
    }

    @Test
    @Order(21)
    public void deleteTransactionForOtherUserTest() {

        long userId = 2L;
        long transactionId = 22L;

        assertThrows( NotFoundException.class, () -> {
            transactionService.delete(transactionId, userId);
        });
    }

    @Test
    @Order(22)
    public void deleteExpenseTypeTransactionTest() {

        long userId = 1L;
        long transactionId = 22L;
        long accountId = 1L;

        transactionService.delete(transactionId, userId);

        Transaction deletedTransaction = em.find(Transaction.class, transactionId);
        assertThat(deletedTransaction).isNull();

        Account account = em.find(Account.class, accountId);
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1550.00"));
        assertThat(account.getTotalExpense()).isEqualTo(new BigDecimal("3450.00"));

        System.out.println(account);
    }

    @Test
    @Order(23)
    public void deleteIncomeTypeTransactionTest() {

        long userId = 1L;
        long transactionId = 28L;
        long accountId = 2L;

        transactionService.delete(transactionId, userId);

        Transaction deletedTransaction = em.find(Transaction.class, transactionId);
        assertThat(deletedTransaction).isNull();

        Account account = em.find(Account.class, accountId);
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("9000.00"));
        assertThat(account.getTotalIncome()).isEqualTo(new BigDecimal("0.00"));

        System.out.println(account);
    }
}
