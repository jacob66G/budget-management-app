package com.example.budget_management_app.recurring_transaction.dao;

import jakarta.persistence.Tuple;

import java.util.List;

public interface RecurringTransactionDao {

    List<Tuple> getSummaryTuplesByUserId(long userId, int page, int limit);

    Long getSummaryTuplesCountByUserId(long userId);
}
