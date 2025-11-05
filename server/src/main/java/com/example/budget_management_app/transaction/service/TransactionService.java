package com.example.budget_management_app.transaction.service;

import com.example.budget_management_app.transaction.domain.SortDirection;
import com.example.budget_management_app.transaction.domain.SortedBy;
import com.example.budget_management_app.transaction.domain.TransactionModeFilter;
import com.example.budget_management_app.transaction.domain.TransactionTypeFilter;
import com.example.budget_management_app.transaction.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    PagedResponse<TransactionSummary> getViews(int page,
                                               int limit,
                                               TransactionTypeFilter type,
                                               TransactionModeFilter mode,
                                               List<Long> accounts,
                                               List<Long> categories,
                                               LocalDate since,
                                               LocalDate to,
                                               SortedBy sortedBy,
                                               SortDirection sortedType,
                                               long id);

    TransactionCreateResponse create(TransactionCreateRequest transactionCreate, long userId);

    TransactionCategoryUpdateResponse changeCategory(long id, long userId, TransactionCategoryUpdateRequest updateReq);

    void update(long Id, long userId, TransactionUpdateRequest req);

    void delete(long Id, long userId);
}
