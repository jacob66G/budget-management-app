package com.example.budget_management_app.recurring_transaction.service;

import com.example.budget_management_app.recurring_transaction.domain.RemovalRange;
import com.example.budget_management_app.recurring_transaction.domain.UpdateRange;
import com.example.budget_management_app.recurring_transaction.dto.*;
import com.example.budget_management_app.transaction.dto.PagedResponse;

public interface RecurringTransactionService {

    PagedResponse<RecurringTransactionSummary> getSummaries(long userId, int page, int limit);

    RecurringTransactionDetailsResponse getDetails(long id, long userId);

    RecurringTransactionCreateResponse create(long userId, RecurringTransactionCreateRequest createReq);

    void changeStatus(long id, long userId, boolean isActive);

    void delete(long id, long userId, RemovalRange range);

    void update(long id, long userId, RecurringTransactionUpdateRequest updateReq, UpdateRange range);

    void generateRecurringTransactions();
}
