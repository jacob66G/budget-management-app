package com.example.budget_management_app.transaction.dao;

import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction.dto.TransactionPaginationParams;
import com.example.budget_management_app.transaction.dto.TransactionFilterParams;
import jakarta.persistence.Tuple;

import java.util.List;
import java.util.Optional;

public interface TransactionDao {

    List<Tuple> getTuples(
            TransactionPaginationParams paginationParams,
            TransactionFilterParams filterParams,
            Long userId
    );

    Long getCount(TransactionFilterParams filterParams, Long userId);

    Transaction save(Transaction transaction);

    void delete(Transaction transaction);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    Optional<Transaction> findByIdAndUserIdAndCategoryId(Long id, Long categoryId, Long userId);

    List<Transaction> findByRecurringTransactionId(Long id);

    boolean existsByCategoryIdAndUserId(Long categoryId, Long userId);

    boolean existsByAccountIdAndUserId(Long accountId, Long userId);

    void reassignCategoryForUser(Long userId, Long oldCategoryId, Long newCategoryId);

    void deleteAllByAccount(Long accountId, Long userId);

    void deleteAllByUser(Long userId);
}
