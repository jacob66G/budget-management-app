package com.example.budget_management_app.transaction.service;

import com.example.budget_management_app.transaction.domain.*;
import com.example.budget_management_app.transaction.dto.PagedResponse;
import com.example.budget_management_app.transaction.dto.TransactionCreate;
import com.example.budget_management_app.transaction.dto.TransactionResponse;
import com.example.budget_management_app.transaction.dto.TransactionView;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    PagedResponse<TransactionView> getTransactionViews(int page,
                                                       int limit,
                                                       TransactionTypeFilter type,
                                                       TransactionModeFilter mode,
                                                       List<Long> accounts,
                                                       LocalDate since,
                                                       LocalDate to,
                                                       SortedBy sortedBy,
                                                       SortDirection sortedType);

    TransactionResponse createTransaction(TransactionCreate transactionCreate, long userId);

    void deleteTransaction(long transactionId, long userId);
}
