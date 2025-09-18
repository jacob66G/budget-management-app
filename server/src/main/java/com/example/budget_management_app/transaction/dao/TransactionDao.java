package com.example.budget_management_app.transaction.dao;

import com.example.budget_management_app.transaction.domain.SortDirection;
import com.example.budget_management_app.transaction.domain.SortedBy;
import com.example.budget_management_app.transaction.domain.TransactionModeFilter;
import com.example.budget_management_app.transaction.domain.TransactionTypeFilter;
import jakarta.persistence.Tuple;

import java.time.LocalDate;
import java.util.List;

public interface TransactionDao {

    List<Tuple> getTransactions(int page,
                                int limit,
                                TransactionTypeFilter type,
                                TransactionModeFilter mode,
                                List<Long> accounts,
                                LocalDate since,
                                LocalDate to,
                                SortedBy sortedBy,
                                SortDirection sortedType
    );

    Long getTransactionsCount(TransactionTypeFilter type,
                              TransactionModeFilter mode,
                              List<Long> accounts,
                              LocalDate since,
                              LocalDate to);
}
