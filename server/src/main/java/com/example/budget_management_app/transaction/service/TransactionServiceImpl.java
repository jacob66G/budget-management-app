package com.example.budget_management_app.transaction.service;

import com.example.budget_management_app.transaction.dao.TransactionDao;
import com.example.budget_management_app.transaction.domain.*;
import com.example.budget_management_app.transaction.dto.PagedResponse;
import com.example.budget_management_app.transaction.dto.TransactionView;
import com.example.budget_management_app.transaction.mapper.Mapper;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService{

    private final TransactionDao transactionDao;

    /**
     * @return TransactionPage object
     */
    @Override
    public PagedResponse<TransactionView> getTransactionViews(int page,
                                                              int limit,
                                                              TransactionTypeFilter type,
                                                              TransactionModeFilter mode,
                                                              List<Long> accounts,
                                                              LocalDate since,
                                                              LocalDate to,
                                                              SortedBy sortedBy,
                                                              SortDirection sortedType) {

        List<Tuple> transactionTuples = transactionDao.getTransactions(
                page,
                limit,
                type,
                mode,
                accounts,
                since,
                to,
                sortedBy,
                sortedType);

        long transactionCount = transactionDao.getTransactionsCount(
                type,
                mode,
                accounts,
                since,
                to);

        return PagedResponse.of(Mapper.toDto(transactionTuples), page, limit, transactionCount);
    }
}
