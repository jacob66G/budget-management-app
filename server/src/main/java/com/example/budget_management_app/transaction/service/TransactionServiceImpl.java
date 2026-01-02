package com.example.budget_management_app.transaction.service;

import com.example.budget_management_app.account.dao.AccountDao;
import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.account.domain.BudgetType;
import com.example.budget_management_app.category.dao.CategoryDao;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.common.exception.CategoryChangeNotAllowedException;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.notification.domain.NotificationType;
import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import com.example.budget_management_app.transaction.dao.TransactionDao;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction.dto.*;
import com.example.budget_management_app.transaction.events.BudgetExceededEvent;
import com.example.budget_management_app.transaction.mapper.TransactionMapper;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;
import com.example.budget_management_app.transaction_common.service.AccountUpdateService;
import com.example.budget_management_app.transaction_common.service.CategoryValidatorService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionDao transactionDao;
    private final CategoryDao categoryDao;
    private final AccountDao accountDao;
    private final CategoryValidatorService transactionValidator;
    private final AccountUpdateService accountUpdateService;
    private final TransactionMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * @return TransactionPage object
     */
    @Override
    public PagedResponse<TransactionSummary> getSummariesPage(
            TransactionPaginationParams paginationParams,
            TransactionFilterParams filterParams,
            Long userId
    ) {

        if (filterParams.getAccountIds() != null && !filterParams.getAccountIds().isEmpty()) {
            this.validateAccountsIds(filterParams.getAccountIds(), userId);
        }

        if (filterParams.getCategoryIds() != null && !filterParams.getCategoryIds().isEmpty()) {
            this.validateCategoryIds(filterParams.getCategoryIds(), userId);
        }

        List<Tuple> transactionTuples = transactionDao.getTuples(
                paginationParams,
                filterParams,
                userId);

        long transactionCount = transactionDao.getCount(
                filterParams,
                userId);

        return PagedResponse.of(mapper.toDto(transactionTuples), paginationParams.getPage(), paginationParams.getLimit(), transactionCount);
    }

    @Transactional
    @Override
    public TransactionCreateResponse create(TransactionCreateRequest createReq, Long userId) {

        // checking if category exists and belongs to the logged user
        long categoryId = createReq.categoryId();
        Category category = categoryDao.findByIdAndUser(categoryId, userId)
                .orElseThrow(() -> new NotFoundException(Category.class.getSimpleName(), categoryId, ErrorCode.NOT_FOUND));

        transactionValidator.validateCategoryType(category.getType(), createReq.type());

        // checking if account exists and belongs to the logged user
        long accountId = createReq.accountId();
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));

        Transaction transaction = mapper.fromDto(createReq);
        transaction.setCategory(category);
        transaction.setAccount(account);

        Transaction savedTransaction = transactionDao.save(transaction);

        accountUpdateService.calculateBalanceAfterTransactionCreation(account, createReq.amount(), createReq.type());

        checkBudgetAndPublishEvent(account, savedTransaction);

        return new TransactionCreateResponse(savedTransaction.getId(), savedTransaction.getTransactionDate());
    }

    @Transactional
    @Override
    public Transaction create(RecurringTransaction recTransaction) {
        Account account = recTransaction.getAccount();

        Transaction transaction = new Transaction(
                recTransaction.getAmount(),
                recTransaction.getTitle(),
                recTransaction.getType(),
                recTransaction.getDescription(),
                LocalDateTime.now()
        );
        transaction.setAccount(account);
        transaction.setCategory(recTransaction.getCategory());

        transaction.setRecurringTransaction(recTransaction);

        Transaction savedTransaction = transactionDao.save(transaction);

        this.accountUpdateService.calculateBalanceAfterTransactionCreation(recTransaction.getAccount(), recTransaction.getAmount(), recTransaction.getType());
        accountDao.update(account);

        checkBudgetAndPublishEvent(account, savedTransaction);

        return savedTransaction;
    }

    @Transactional
    @Override
    public TransactionCategoryChangeResponse changeCategory(Long id, TransactionCategoryChangeRequest updateReq, Long userId) {

        Transaction transaction = transactionDao.findByIdAndUserIdAndCategoryId(id, updateReq.currentCategoryId(), userId)
                .orElseThrow(() -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        if (transaction.getRecurringTransaction() != null) {
            throw new CategoryChangeNotAllowedException(transaction.getId(), ErrorCode.TRANSACTION_IS_RECURRING);
        }

        long newCategoryId = updateReq.newCategoryId();
        Category category = categoryDao.findByIdAndUser(newCategoryId, userId)
                .orElseThrow(() -> new NotFoundException(Category.class.getSimpleName(), newCategoryId, ErrorCode.NOT_FOUND));

        transactionValidator.validateCategoryType(category.getType(), transaction.getType());
        transaction.setCategory(category);

        return new TransactionCategoryChangeResponse(newCategoryId, category.getName(), category.getIconKey());
    }

    @Transactional
    @Override
    public void update(Long id, TransactionUpdateRequest req, Long userId) {

        Transaction transaction = transactionDao.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        if (!req.amount().equals(transaction.getAmount())) {
            BigDecimal newAmount = req.amount();
            accountUpdateService.calculateBalanceAfterTransactionUpdate(transaction.getAccount(), newAmount, transaction.getAmount(), transaction.getType());
            transaction.setAmount(newAmount);
        }

        transaction.setTitle(req.title());
        transaction.setDescription(req.description());
    }

    @Transactional
    @Override
    public void delete(Long id, Long userId) {

        Transaction transaction = transactionDao.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        accountUpdateService.calculateBalanceAfterTransactionDeletion(transaction.getAccount(), transaction.getAmount(), transaction.getType());
        transactionDao.delete(transaction);
    }

    private void validateAccountsIds(List<Long> accountIds, Long userId) {

        Set<Long> userAccountIdsSet = new HashSet<>(accountDao.getUserAccountIds(userId));
        List<Long> differences = accountIds.stream()
                .filter(id -> !userAccountIdsSet.contains(id))
                .toList();

        if (!differences.isEmpty()) {
            throw new NotFoundException(Account.class.getSimpleName(), differences, ErrorCode.NOT_FOUND);
        }
    }

    private void validateCategoryIds(List<Long> categoryIds, Long userId) {
        Set<Long> userCategoryIdsSet = new HashSet<>(categoryDao.getUserCategoryIds(userId));
        List<Long> differences = categoryIds.stream().
                filter(id -> !userCategoryIdsSet.contains(id))
                .toList();

        if (!differences.isEmpty()) {
            throw new NotFoundException(Category.class.getSimpleName(), differences, ErrorCode.NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCategoryAndUser(Long categoryId, Long userId) {
        return transactionDao.existsByCategoryIdAndUserId(categoryId, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByAccountAndUser(Long accountId, Long userId) {
        return transactionDao.existsByAccountIdAndUserId(accountId, userId);
    }

    @Transactional
    @Override
    public void reassignCategoryForUser(Long userId, Long oldCategoryId, Long newCategoryId) {
        transactionDao.reassignCategoryForUser(userId, oldCategoryId, newCategoryId);
        log.info("User: {} has reassigned category in transactions from: {} to: {}", userId, oldCategoryId, newCategoryId);
    }

    @Transactional
    @Override
    public void deleteAllByAccount(Long accountId, Long userId) {
        transactionDao.deleteAllByAccount(accountId, userId);
        log.info("User: {} has deleted transactions for account: {}.", userId, accountId);
    }

    @Override
    public void deleteAllByUser(Long userId) {
        transactionDao.deleteAllByUser(userId);
        log.info("User: {} has deleted ALL transactions.", userId);
    }

    private void checkBudgetAndPublishEvent(Account account, Transaction transaction) {
        if (transaction.getType() != TransactionType.EXPENSE || account.getBudgetType() == BudgetType.NONE) {
            return;
        }

        BigDecimal limit = account.getBudget();
        BigDecimal currentUsageInPeriod = calculateCurrentUsage(account);

        if (limit != null && currentUsageInPeriod.compareTo(limit) > 0) {
            eventPublisher.publishEvent(new BudgetExceededEvent(
                    account.getUser().getId(),
                    NotificationType.ERROR,
                    account.getId(),
                    account.getName(),
                    transaction.getAmount(),
                    currentUsageInPeriod,
                    limit
            ));
            return;
        }

        if (limit != null && account.getAlertThreshold() != null) {
            BigDecimal thresholdAmount = limit.multiply(BigDecimal.valueOf(account.getAlertThreshold() / 100.0));

            if (currentUsageInPeriod.compareTo(thresholdAmount) > 0) {
                eventPublisher.publishEvent(new BudgetExceededEvent(
                        account.getUser().getId(),
                        NotificationType.WARNING,
                        account.getId(),
                        account.getName(),
                        transaction.getAmount(),
                        currentUsageInPeriod,
                        limit
                ));
            }
        }
    }

    private BigDecimal calculateCurrentUsage(Account account) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end = now;

        if (account.getBudgetType() == BudgetType.MONTHLY) {
            start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        } else if (account.getBudgetType() == BudgetType.WEEKLY) {
            start = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        } else {
            return BigDecimal.ZERO;
        }

        return transactionDao.getSumForAccountInPeriod(account.getId(), start, end, TransactionType.EXPENSE);
    }
}
