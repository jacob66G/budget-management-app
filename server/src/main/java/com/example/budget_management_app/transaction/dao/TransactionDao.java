package com.example.budget_management_app.transaction.dao;

import com.example.budget_management_app.transaction.domain.*;
import jakarta.persistence.Tuple;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    Transaction saveTransaction(Transaction transaction);

    void deleteTransaction(Transaction transaction);

    Optional<Transaction> findByIdAndUserId(long id, long userId);
}
