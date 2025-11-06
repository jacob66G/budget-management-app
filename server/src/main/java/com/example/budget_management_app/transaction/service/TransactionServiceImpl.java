package com.example.budget_management_app.transaction.service;

import com.example.budget_management_app.account.dao.AccountDao;
import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.dao.CategoryDao;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.common.exception.CategoryChangeNotAllowedException;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.transaction.dao.TransactionDao;
import com.example.budget_management_app.transaction.domain.*;
import com.example.budget_management_app.transaction.dto.*;
import com.example.budget_management_app.transaction.mapper.Mapper;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;
import com.example.budget_management_app.transaction_common.service.AccountUpdateService;
import com.example.budget_management_app.transaction_common.service.CategoryValidatorService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
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
    public PagedResponse<TransactionSummary> getViews(int page,
                                                      int limit,
                                                      TransactionTypeFilter type,
                                                      TransactionModeFilter mode,
                                                      List<Long> accounts,
                                                      List<Long> categories,
                                                      LocalDate since,
                                                      LocalDate to,
                                                      SortedBy sortedBy,
                                                      SortDirection sortedType,
                                                      long userId) {

        if(!accountDao.areAccountsBelongToUser(userId, accounts)) {
            throw new NotFoundException(Account.class.getSimpleName(), accounts, ErrorCode.NOT_FOUND);
        }

        if (!categoryDao.areCategoriesBelongToUser(userId, categories)) {
            throw new NotFoundException(Category.class.getSimpleName(), categories, ErrorCode.NOT_FOUND);
        }

        List<Tuple> transactionTuples = transactionDao.getTuples(
                page,
                limit,
                type,
                mode,
                accounts,
                categories,
                since,
                to,
                sortedBy,
                sortedType);

        long transactionCount = transactionDao.getCount(
                type,
                mode,
                accounts,
                categories,
                since,
                to);

        return PagedResponse.of(Mapper.toDto(transactionTuples), page, limit, transactionCount);
    }

    @Transactional
    @Override
    public TransactionCreateResponse create(TransactionCreateRequest createReq, long userId) {

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
    public TransactionCategoryUpdateResponse changeCategory(long id, long userId, TransactionCategoryUpdateRequest updateReq) {

        Transaction transaction = transactionDao.findByIdAndUserIdAndCategoryId(id, userId, updateReq.currentTransactionCategoryId())
                .orElseThrow( () -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        if (transaction.getRecurringTransaction() != null) {
            throw new CategoryChangeNotAllowedException(transaction.getId(), ErrorCode.TRANSACTION_IS_RECURRING);
        }

        long newCategoryId = updateReq.newTransactionCategoryId();
        Category category = categoryDao.findByIdAndUser(newCategoryId, userId)
                .orElseThrow( () -> new NotFoundException(Category.class.getSimpleName(), newCategoryId, ErrorCode.NOT_FOUND));

        transactionValidator.validateCategoryType(category.getType(), transaction.getType());

        transaction.removeCategory();
        transaction.setCategory(category);

        return new TransactionCategoryUpdateResponse(newCategoryId, category.getName(), category.getIconPath());
    }

    @Transactional
    @Override
    public void update(long id, long userId, TransactionUpdateRequest req) {

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
    public void delete(long id, long userId) {

        Transaction transaction = transactionDao.findByIdAndUserId(id, userId)
                .orElseThrow( () -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        accountUpdateService.calculateBalanceAfterTransactionDeletion(transaction.getAccount(), transaction.getAmount(), transaction.getType());
        transaction.removeCategory();
        transaction.removeAccount();
        transactionDao.delete(transaction);
    }


}
