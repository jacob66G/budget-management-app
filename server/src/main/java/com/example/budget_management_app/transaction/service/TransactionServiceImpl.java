package com.example.budget_management_app.transaction.service;

import com.example.budget_management_app.account.dao.AccountDao;
import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.dao.CategoryDao;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.common.exception.CategoryChangeNotAllowedException;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.transaction.dao.TransactionDao;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction.dto.*;
import com.example.budget_management_app.transaction.mapper.Mapper;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;
import com.example.budget_management_app.transaction_common.service.AccountUpdateService;
import com.example.budget_management_app.transaction_common.service.CategoryValidatorService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService{

    private final TransactionDao transactionDao;
    private final CategoryDao categoryDao;
    private final AccountDao accountDao;
    private final CategoryValidatorService transactionValidator;
    private final AccountUpdateService accountUpdateService;

    /**
     * @return TransactionPage object
     */
    @Override
    public PagedResponse<TransactionSummary> getSummariesPage(
            TransactionPaginationParams paginationParams,
            TransactionFilterParams filterParams,
            Long userId
    ) {

        if(!accountDao.areAccountsBelongToUser(userId, filterParams.getAccountIds())) {
            throw new NotFoundException(Account.class.getSimpleName(), filterParams.getAccountIds(), ErrorCode.NOT_FOUND);
        }

        if (!categoryDao.areCategoriesBelongToUser(userId, filterParams.getCategoryIds())) {
            throw new NotFoundException(Category.class.getSimpleName(), filterParams.getCategoryIds(), ErrorCode.NOT_FOUND);
        }

        List<Tuple> transactionTuples = transactionDao.getTuples(
                paginationParams,
                filterParams);

        long transactionCount = transactionDao.getCount(
                filterParams);

        return PagedResponse.of(Mapper.toDto(transactionTuples), paginationParams.getPage(), paginationParams.getLimit(), transactionCount);
    }

    @Transactional
    @Override
    public TransactionCreateResponse create(TransactionCreateRequest createReq, Long userId) {

        // checking if category exists and belongs to the logged user
        long categoryId = createReq.categoryId();
        Category category = categoryDao.findByIdAndUser(categoryId, userId)
                .orElseThrow( () -> new NotFoundException(Category.class.getSimpleName(), categoryId, ErrorCode.NOT_FOUND));

        transactionValidator.validateCategoryType(category.getType(), createReq.type());

        // checking if account exists and belongs to the logged user
        long accountId = createReq.accountId();
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow( () -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));

        accountUpdateService.calculateBalanceAfterTransactionCreation(account, createReq.amount(), createReq.type());
        Transaction transaction = Mapper.fromDto(createReq);
        transaction.setCategory(category);
        transaction.setAccount(account);

        Transaction savedTransaction = transactionDao.save(transaction);

        return new TransactionCreateResponse(savedTransaction.getId(), savedTransaction.getTransactionDate());
    }

    @Transactional
    @Override
    public TransactionCategoryChangeResponse changeCategory(Long id, TransactionCategoryChangeRequest updateReq, Long userId) {

        Transaction transaction = transactionDao.findByIdAndUserIdAndCategoryId(id, updateReq.currentCategoryId(), userId)
                .orElseThrow( () -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        if (transaction.getRecurringTransaction() != null) {
            throw new CategoryChangeNotAllowedException(transaction.getId(), ErrorCode.TRANSACTION_IS_RECURRING);
        }

        long newCategoryId = updateReq.newCategoryId();
        Category category = categoryDao.findByIdAndUser(newCategoryId, userId)
                .orElseThrow( () -> new NotFoundException(Category.class.getSimpleName(), newCategoryId, ErrorCode.NOT_FOUND));

        transactionValidator.validateCategoryType(category.getType(), transaction.getType());

        transaction.removeCategory();
        transaction.setCategory(category);

        return new TransactionCategoryChangeResponse(newCategoryId, category.getName(), category.getIconKey());
    }

    @Transactional
    @Override
    public void update(Long id, TransactionUpdateRequest req, Long userId) {

        Transaction transaction = transactionDao.findByIdAndUserId(id, userId)
                .orElseThrow( () -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

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
                .orElseThrow( () -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        accountUpdateService.calculateBalanceAfterTransactionDeletion(transaction.getAccount(), transaction.getAmount(), transaction.getType());
        transaction.removeCategory();
        transaction.removeAccount();
        transactionDao.delete(transaction);
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

}
