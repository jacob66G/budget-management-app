package com.example.budget_management_app.transaction.service;

import com.example.budget_management_app.account.dao.AccountDao;
import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.dao.CategoryDao;
import com.example.budget_management_app.category.domain.Category;
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
    public PagedResponse<TransactionView> getTransactionViews(int page,
                                                              int limit,
                                                              TransactionTypeFilter type,
                                                              TransactionModeFilter mode,
                                                              List<Long> accounts,
                                                              LocalDate since,
                                                              LocalDate to,
                                                              SortedBy sortedBy,
                                                              SortDirection sortedType) {

        List<Tuple> transactionTuples = transactionDao.getTransactions(
                page,
                limit,
                type,
                mode,
                accounts,
                since,
                to,
                sortedBy,
                sortedType);

        long transactionCount = transactionDao.getTransactionsCount(
                type,
                mode,
                accounts,
                since,
                to);

        return PagedResponse.of(Mapper.toDto(transactionTuples), page, limit, transactionCount);
    }

    @Transactional
    @Override
    public TransactionResponse createTransaction(TransactionCreateRequest transactionCreate, long userId) {

        // checking if category exists and belongs to the logged user
        long categoryId = transactionCreate.categoryId();
        Category category = categoryDao.findByIdAndUser(categoryId, userId)
                .orElseThrow( () -> new NotFoundException(Category.class.getSimpleName(), categoryId, ErrorCode.NOT_FOUND));

        // checking if account exists and belongs to the logged user
        long accountId = transactionCreate.accountId();
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow( () -> new NotFoundException(Account.class.getSimpleName(), accountId, ErrorCode.NOT_FOUND));

        Transaction transaction = Mapper.fromDto(transactionCreate);
        transaction.setCategory(category);
        transaction.setAccount(account);

        Transaction savedTransaction = transactionDao.saveTransaction(transaction);

        return new TransactionResponse(savedTransaction.getId(), savedTransaction.getTransactionDate());
    }

    @Transactional
    @Override
    public TransactionCategoryUpdateResponse updateTransactionCategory(long id, long userId, TransactionCategoryUpdateRequest updateReq) {

        Transaction transaction = transactionDao.findByIdAndUserIdAndCategoryId(id, userId, updateReq.currentTransactionCategoryId())
                .orElseThrow( () -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        long newCategoryId = updateReq.newTransactionCategoryId();
        Category category = categoryDao.findByIdAndUser(newCategoryId, userId)
                .orElseThrow( () -> new NotFoundException(Category.class.getSimpleName(), newCategoryId, ErrorCode.NOT_FOUND));

        transaction.removeCategory();
        transaction.setCategory(category);

        return new TransactionCategoryUpdateResponse(newCategoryId, category.getName(), category.getIconPath());
    }

    @Transactional
    @Override
    public void updateTransaction(long id, long userId, TransactionUpdateRequest req) {

        Transaction transaction = transactionDao.findByIdAndUserId(id, userId)
                .orElseThrow( () -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        transaction.setAmount(req.amount());
        transaction.setTitle(req.title());
        transaction.setDescription(req.description());
    }

    @Transactional
    @Override
    public void deleteTransaction(long id, long userId) {

        Transaction transaction = transactionDao.findByIdAndUserId(id, userId)
                .orElseThrow( () -> new NotFoundException(Transaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        transaction.removeCategory();
        transaction.removeAccount();
        transactionDao.deleteTransaction(transaction);
    }
}
