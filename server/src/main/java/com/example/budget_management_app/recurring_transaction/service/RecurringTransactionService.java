package com.example.budget_management_app.recurring_transaction.service;

import com.example.budget_management_app.recurring_transaction.domain.RemovalRange;
import com.example.budget_management_app.recurring_transaction.dto.RecurringTransactionCreateRequest;
import com.example.budget_management_app.recurring_transaction.dto.RecurringTransactionCreateResponse;
import com.example.budget_management_app.recurring_transaction.dto.RecurringTransactionDetailsResponse;
import com.example.budget_management_app.recurring_transaction.dto.RecurringTransactionSummary;
import com.example.budget_management_app.transaction.dto.PagedResponse;

public interface RecurringTransactionService {

    PagedResponse<RecurringTransactionSummary> getSummaries(long userId, int page, int limit);

    RecurringTransactionDetailsResponse getDetails(long id, long userId);

    RecurringTransactionCreateResponse create(long userId, RecurringTransactionCreateRequest createReq);

    void changeStatus(long id, long userId, boolean isActive);

    void delete(long id, long userId, RemovalRange range);
}
