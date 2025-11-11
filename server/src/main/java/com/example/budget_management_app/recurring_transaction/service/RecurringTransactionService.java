package com.example.budget_management_app.recurring_transaction.service;

import com.example.budget_management_app.recurring_transaction.domain.RemovalRange;
import com.example.budget_management_app.recurring_transaction.domain.UpdateRange;
import com.example.budget_management_app.recurring_transaction.dto.*;
import com.example.budget_management_app.transaction_common.dto.PageRequest;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;

public interface RecurringTransactionService {

    PagedResponse<RecurringTransactionSummary> getSummariesPage(PageRequest pageRequest, Long userId);

    RecurringTransactionDetailsResponse getDetails(Long id, Long userId);

    PagedResponse<UpcomingTransactionSummary> getUpcomingTransactionsPage(PageRequest pageRequest,
                                                                               UpcomingTransactionSearchCriteria searchCriteria,
                                                                               Long userId);

    RecurringTransactionCreateResponse create(RecurringTransactionCreateRequest createReq, Long userId);

    void changeStatus(Long id, Boolean isActive, Long userId);

    void delete(Long id, RemovalRange range, Long userId);

    void update(Long id, RecurringTransactionUpdateRequest updateReq, UpdateRange range, Long userId);

    void generateRecurringTransactions();
}
