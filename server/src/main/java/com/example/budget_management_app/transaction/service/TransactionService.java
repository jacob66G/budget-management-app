package com.example.budget_management_app.transaction.service;

import com.example.budget_management_app.transaction.domain.*;
import com.example.budget_management_app.transaction.dto.*;

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

    TransactionResponse createTransaction(TransactionCreateRequest transactionCreate, long userId);

    TransactionCategoryUpdateResponse updateTransactionCategory(long id, long userId, TransactionCategoryUpdateRequest updateReq);

    void updateTransaction(long Id, long userId, TransactionUpdateRequest req);

    void deleteTransaction(long Id, long userId);
}
