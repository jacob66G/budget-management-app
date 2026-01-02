package com.example.budget_management_app.transaction.service;

import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction.dto.*;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;

public interface TransactionService {

    PagedResponse<TransactionSummary> getSummariesPage(
            TransactionPaginationParams paginationParams,
            TransactionFilterParams filterParams,
            Long userId
    );

    TransactionCreateResponse create(TransactionCreateRequest transactionCreate, Long userId);

    Transaction create(RecurringTransaction recTransaction);

    TransactionCategoryChangeResponse changeCategory(Long id, TransactionCategoryChangeRequest updateReq, Long userId);

    void update(Long id, TransactionUpdateRequest req, Long userId);

    void delete(Long id, Long userId);

    boolean existsByCategoryAndUser(Long categoryId, Long userId);

    boolean existsByAccountAndUser(Long accountId, Long userId);

    void reassignCategoryForUser(Long userId, Long oldCategoryId, Long newCategoryId);

    void deleteAllByAccount(Long accountId, Long userId);

    void deleteAllByUser(Long userId);
}
