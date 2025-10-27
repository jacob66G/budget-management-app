package com.example.budget_management_app.recurring_transaction.service;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.recurring_transaction.dao.RecurringTransactionDao;
import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import com.example.budget_management_app.recurring_transaction.dto.RecurringTransactionDetailsResponse;
import com.example.budget_management_app.recurring_transaction.dto.RecurringTransactionSummary;
import com.example.budget_management_app.recurring_transaction.mapper.Mapper;
import com.example.budget_management_app.transaction.dto.PagedResponse;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RecurringTransactionServiceImpl implements RecurringTransactionService{

    private final RecurringTransactionDao recurringTransactionDao;
    /**
     * @param userId
     * @return
     */
    @Override
    public PagedResponse<RecurringTransactionSummary> getSummaries(long userId, int page, int limit) {

        List<Tuple> results = recurringTransactionDao.getSummaryTuplesByUserId(
                userId, page, limit
        );

        long recTransactionsCount = recurringTransactionDao.getSummaryTuplesCountByUserId(userId);

        return PagedResponse.of(Mapper.fromTuples(results), page, limit, recTransactionsCount);
    }

    /**
     * @param id
     * @param userId
     * @return
     */
    @Override
    public RecurringTransactionDetailsResponse getDetails(long id, long userId) {

        RecurringTransaction recurringTransaction = recurringTransactionDao.findByIdAndUserId(id, userId)
                .orElseThrow( () -> new NotFoundException(RecurringTransaction.class.getSimpleName(), id, ErrorCode.NOT_FOUND));

        return Mapper.toDetails(recurringTransaction);
    }
}
