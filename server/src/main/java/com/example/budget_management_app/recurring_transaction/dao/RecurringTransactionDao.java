package com.example.budget_management_app.recurring_transaction.dao;

import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import jakarta.persistence.Tuple;

import java.util.List;
import java.util.Optional;

public interface RecurringTransactionDao {

    List<Tuple> getSummaryTuplesByUserId(long userId, int page, int limit);

    Long getSummaryTuplesCountByUserId(long userId);

    Optional<RecurringTransaction> findByIdAndUserId(long id, long userId);

    RecurringTransaction create(RecurringTransaction recurringTransaction);

    void delete(RecurringTransaction recurringTransaction);
}
