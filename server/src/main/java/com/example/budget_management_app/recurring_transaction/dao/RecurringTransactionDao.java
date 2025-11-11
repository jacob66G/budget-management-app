package com.example.budget_management_app.recurring_transaction.dao;

import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import com.example.budget_management_app.recurring_transaction.dto.UpcomingTransactionSearchCriteria;
import com.example.budget_management_app.transaction_common.dto.PageRequest;
import jakarta.persistence.Tuple;

import java.util.List;
import java.util.Optional;

public interface RecurringTransactionDao {

    List<Tuple> getSummaryTuplesByUserId(PageRequest pageReq, Long userId);

    Long getSummaryTuplesCountByUserId(Long userId);

    Optional<RecurringTransaction> findByIdAndUserId(Long id, Long userId);

    RecurringTransaction create(RecurringTransaction recurringTransaction);

    void delete(RecurringTransaction recurringTransaction);

    List<RecurringTransaction> searchForRecurringTransactionsToCreate();

    List<Tuple> getUpcomingTransactionsTuples(PageRequest pageRequest,
                                              UpcomingTransactionSearchCriteria searchCriteria);

    Long getUpcomingTransactionsCount(UpcomingTransactionSearchCriteria searchCriteria);
}
