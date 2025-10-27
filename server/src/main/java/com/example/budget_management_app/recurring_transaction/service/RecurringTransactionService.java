package com.example.budget_management_app.recurring_transaction.service;

import com.example.budget_management_app.recurring_transaction.dto.RecurringTransactionDetailsResponse;
import com.example.budget_management_app.recurring_transaction.dto.RecurringTransactionSummary;
import com.example.budget_management_app.transaction.dto.PagedResponse;

public interface RecurringTransactionService {

    PagedResponse<RecurringTransactionSummary> getSummaries(long userId, int page, int limit);

    RecurringTransactionDetailsResponse getDetails(long id, long userId);
}
