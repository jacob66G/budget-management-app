package com.example.budget_management_app.recurring_transaction.service;

import com.example.budget_management_app.account.dao.AccountDao;
import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.dao.CategoryDao;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.InternalException;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.StatusAlreadySetException;
import com.example.budget_management_app.recurring_transaction.dao.RecurringTransactionDao;
import com.example.budget_management_app.recurring_transaction.domain.RecurringInterval;
import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import com.example.budget_management_app.recurring_transaction.domain.RemovalRange;
import com.example.budget_management_app.recurring_transaction.domain.UpdateRange;
import com.example.budget_management_app.recurring_transaction.dto.*;
import com.example.budget_management_app.recurring_transaction.mapper.Mapper;
import com.example.budget_management_app.transaction.dao.TransactionDao;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;
import com.example.budget_management_app.transaction_common.service.AccountUpdateService;
import com.example.budget_management_app.transaction_common.service.CategoryValidatorService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class RecurringTransactionServiceImpl implements RecurringTransactionService{

    private final RecurringTransactionDao recurringTransactionDao;
    private final TransactionDao transactionDao;
    private final CategoryValidatorService transactionValidator;
    private final AccountUpdateService accountUpdateService;
    private final AccountDao accountDao;
    private final CategoryDao categoryDao;
    /**
     * @param userId
     * @return
     */
    @Override
    public PagedResponse<RecurringTransactionSummary> getSummaries(long userId, int page, int limit) {

        List<Tuple> results = recurringTransactionDao.getSummaryTuplesByUserId(
                userId, page, limit
        );

        long recTransactionsCount = recurringTransactionDao.getSummaryTuplesCountByUserId(userId);

        return PagedResponse.of(Mapper.fromTuples(results), page, limit, recTransactionsCount);
    }

    /**
     * @param id
     * @param userId
     * @return
     */
    @Override
    public RecurringTransactionDetailsResponse getDetails(long id, long userId) {

        RecurringTransaction recurringTransaction = recurringTransactionDao.findByIdAndUserId(id, userId)
                .orElseThrow( () -> new NotFoundException(RecurringTransaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        return Mapper.toDetails(recurringTransaction);
    }

    /**
     * @param userId
     * @return
     */
    @Transactional
    @Override
    public RecurringTransactionCreateResponse create(long userId, RecurringTransactionCreateRequest createReq) {

        long accountId = createReq.accountId();
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow( () -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));

        long categoryId = createReq.categoryId();
        Category category = categoryDao.findByIdAndUser(categoryId, userId)
                .orElseThrow( () -> new NotFoundException(Category.class.getSimpleName(), categoryId, ErrorCode.NOT_FOUND));

        transactionValidator.validateCategoryType(category.getType(), createReq.type());

        RecurringInterval interval = createReq.recurringInterval();
        int recurringValue = createReq.recurringValue();
        LocalDate startDate = createReq.startDate();
        LocalDate nextOccurrence;
        if (startDate.isEqual(LocalDate.now()) && LocalTime.now().isAfter(LocalTime.NOON)) {
            nextOccurrence = calculateNextOccurrence(interval, recurringValue, LocalDate.now());
            RecurringTransaction recurringTransaction = new RecurringTransaction(
                    createReq.amount(), createReq.title(), createReq.type(), createReq.description(), startDate, createReq.endDate(),
                    interval, recurringValue, nextOccurrence, true, LocalDateTime.now()
            );
            Transaction transaction = new Transaction(
                          createReq.amount(), createReq.title(), createReq.type(), createReq.description(), LocalDateTime.now()
            );
            transaction.setAccount(account);
            transaction.setCategory(category);

            recurringTransaction.addTransaction(transaction);
            recurringTransaction.setAccount(account);
            recurringTransaction.setCategory(category);

            this.accountUpdateService.calculateBalanceAfterTransactionCreation(account, createReq.amount(), createReq.type());

            RecurringTransaction createdRecurringTransaction = recurringTransactionDao.create(recurringTransaction);

            Transaction createdTransaction = createdRecurringTransaction.getTransactions()
                    .stream()
                    .findFirst()
                    .orElseThrow( () -> new InternalException("No assign transaction to recurring transaction"));

            return new RecurringTransactionCreateResponse(
                    createdRecurringTransaction.getId(),
                    createdRecurringTransaction.getNextOccurrence(),
                    true,
                    true,
                    Mapper.toTransactionView(createdTransaction)
            );

        } else {

            nextOccurrence = startDate;
            RecurringTransaction recurringTransaction = new RecurringTransaction(
                    createReq.amount(), createReq.title(), createReq.type(), createReq.description(), startDate, createReq.endDate(),
                    interval, recurringValue, nextOccurrence, true, LocalDateTime.now()
            );

            recurringTransaction.setAccount(account);
            recurringTransaction.setCategory(category);

            RecurringTransaction createdRecurringTransaction = recurringTransactionDao.create(recurringTransaction);

            return new RecurringTransactionCreateResponse(
                    createdRecurringTransaction.getId(),
                    createdRecurringTransaction.getNextOccurrence(),
                    true,
                    false,
                    null
            );
        }
    }

    /**
     * @param id
     * @param userId
     * @param isActive
     */
    @Transactional
    @Override
    public void changeStatus(long id, long userId, boolean isActive) {

        RecurringTransaction recurringTransaction = recurringTransactionDao.findByIdAndUserId(id, userId)
                .orElseThrow( () -> new NotFoundException(RecurringTransaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        if (recurringTransaction.isActive() == isActive) {
            throw new StatusAlreadySetException(RecurringTransaction.class.getSimpleName(), recurringTransaction.getId(), (isActive) ? "active" : "not active" , ErrorCode.STATUS_ALREADY_SET);
        }

        if (isActive) {

            if (recurringTransaction.getNextOccurrence().isEqual(LocalDate.now()) && LocalTime.now().isAfter(LocalTime.NOON)) {
                LocalDate nextOccurrence = this.calculateNextOccurrence(recurringTransaction.getRecurringInterval(), recurringTransaction.getRecurringValue(), LocalDate.now());
                recurringTransaction.setNextOccurrence(nextOccurrence);
            } else if (recurringTransaction.getNextOccurrence().isBefore(LocalDate.now())) {
                LocalDate incrementDate = recurringTransaction.getNextOccurrence();
                while(!(incrementDate.isAfter(LocalDate.now()) || incrementDate.isEqual(LocalDate.now()))) {
                    incrementDate = this.calculateNextOccurrence(recurringTransaction.getRecurringInterval(), recurringTransaction.getRecurringValue(), incrementDate);
                }

                if (incrementDate.isAfter(LocalDate.now()) || (incrementDate.isEqual(LocalDate.now()) && LocalTime.now().isBefore(LocalTime.NOON))){
                    recurringTransaction.setNextOccurrence(incrementDate);
                } else {
                    LocalDate nextOccurrence = this.calculateNextOccurrence(recurringTransaction.getRecurringInterval(), recurringTransaction.getRecurringValue(), LocalDate.now());
                    recurringTransaction.setNextOccurrence(nextOccurrence);
                }
            }
        }

        recurringTransaction.setActive(isActive);
    }

    /**
     * @param id
     * @param userId
     * @param range
     */
    @Transactional
    @Override
    public void delete(long id, long userId, RemovalRange range) {

        RecurringTransaction recurringTransaction = recurringTransactionDao.findByIdAndUserId(id, userId)
                .orElseThrow( () -> new NotFoundException(RecurringTransaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        if (range.equals(RemovalRange.TEMPLATE)) {
            recurringTransaction.detachTransactions();
        } else {
            if (recurringTransaction.getTransactions() != null && !recurringTransaction.getTransactions().isEmpty()) {
                BigDecimal totalAmount = recurringTransaction.getTransactions()
                        .stream()
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                this.accountUpdateService.calculateBalanceAfterTransactionDeletion(recurringTransaction.getAccount(), totalAmount, recurringTransaction.getType());
            }
        }

        recurringTransaction.setAccount(null);
        recurringTransaction.setCategory(null);

        recurringTransactionDao.delete(recurringTransaction);
    }

    /**
     * @param id
     * @param userId
     * @param updateReq
     */
    @Transactional
    @Override
    public void update(long id, long userId, RecurringTransactionUpdateRequest updateReq, UpdateRange range) {

        RecurringTransaction recurringTransaction = recurringTransactionDao.findByIdAndUserId(id, userId)
                .orElseThrow( () -> new NotFoundException(RecurringTransaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        recurringTransaction.setTitle(updateReq.title());
        recurringTransaction.setDescription(updateReq.description());

        if (recurringTransaction.getAmount().compareTo(updateReq.amount()) != 0) {
            recurringTransaction.setAmount(updateReq.amount());
            if (range.equals(UpdateRange.ALL)) {
                Set<Transaction> transactions = recurringTransaction.getTransactions();
                if (transactions != null && !transactions.isEmpty()) {
                    BigDecimal currentTotalAmount = transactions.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal newTotalAmount = updateReq.amount().multiply(BigDecimal.valueOf(transactions.size()));
                    this.accountUpdateService.calculateBalanceAfterTransactionUpdate(
                            recurringTransaction.getAccount(),
                            newTotalAmount,
                            currentTotalAmount,
                            recurringTransaction.getType()
                    );
                    transactions.forEach( transaction -> {
                        transaction.setAmount(updateReq.amount());
                        transaction.setTitle(updateReq.title());
                        transaction.setDescription(updateReq.description());
                    });
                }
            }
        }
    }

    @Transactional
    @Override
    public void generateRecurringTransactions() {

        List<RecurringTransaction> recurringTransactionsToCreate = recurringTransactionDao.searchForRecurringTransactionsToCreate();

        if (!recurringTransactionsToCreate.isEmpty()) {
            recurringTransactionsToCreate.forEach(
                    recTransaction -> {
                        LocalDate nextOccurrence = this.calculateNextOccurrence(recTransaction.getRecurringInterval(), recTransaction.getRecurringValue(), LocalDate.now());
                        recTransaction.setNextOccurrence(nextOccurrence);
                        Transaction transaction = new Transaction(
                                recTransaction.getAmount(), recTransaction.getTitle(), recTransaction.getType(),
                                recTransaction.getDescription(), LocalDateTime.now()
                        );
                        this.accountUpdateService.calculateBalanceAfterTransactionCreation(recTransaction.getAccount(), recTransaction.getAmount(), recTransaction.getType());
                        transaction.setAccount(recTransaction.getAccount());
                        transaction.setCategory(recTransaction.getCategory());
                        recTransaction.addTransaction(transaction);
                    }
            );
        }
    }

    private LocalDate calculateNextOccurrence(RecurringInterval interval, int recurringValue, LocalDate relativeDate) {
        LocalDate nextOccurrence;
        if (interval.equals(RecurringInterval.DAY)) {
            nextOccurrence = relativeDate.plusDays(recurringValue);
        } else if (interval.equals(RecurringInterval.WEEK)) {
            nextOccurrence = relativeDate.plusWeeks(recurringValue);
        } else if (interval.equals(RecurringInterval.MONTH)) {
            nextOccurrence = relativeDate.plusMonths(recurringValue);
        } else {
            nextOccurrence = relativeDate.plusYears(recurringValue);
        }
        return nextOccurrence;
    }
}
