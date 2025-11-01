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
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService{

    private final TransactionDao transactionDao;
    private final CategoryDao categoryDao;
    private final AccountDao accountDao;

    /**
     * @return TransactionPage object
     */
    @Override
    public PagedResponse<TransactionView> getViews(int page,
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
    public TransactionResponse create(TransactionCreateRequest createReq, long userId) {

        // checking if category exists and belongs to the logged user
        long categoryId = createReq.categoryId();
        Category category = categoryDao.findByIdAndUser(categoryId, userId)
                .orElseThrow( () -> new NotFoundException(Category.class.getSimpleName(), categoryId, ErrorCode.NOT_FOUND));

        // checking if account exists and belongs to the logged user
        long accountId = createReq.accountId();
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow( () -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));

        Transaction transaction = Mapper.fromDto(createReq);
        transaction.setCategory(category);
        transaction.setAccount(account);

        Transaction savedTransaction = transactionDao.save(transaction);

        return new TransactionResponse(savedTransaction.getId(), savedTransaction.getTransactionDate());
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

        transaction.removeCategory();
        transaction.setCategory(category);

        return new TransactionCategoryUpdateResponse(newCategoryId, category.getName(), category.getIconPath());
    }

    @Transactional
    @Override
    public void update(long id, long userId, TransactionUpdateRequest req) {

        Transaction transaction = transactionDao.findByIdAndUserId(id, userId)
                .orElseThrow( () -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        transaction.setAmount(req.amount());
        transaction.setTitle(req.title());
        transaction.setDescription(req.description());
    }

    @Transactional
    @Override
    public void delete(long id, long userId) {

        Transaction transaction = transactionDao.findByIdAndUserId(id, userId)
                .orElseThrow( () -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        transaction.removeCategory();
        transaction.removeAccount();
        transactionDao.delete(transaction);
    }
}
