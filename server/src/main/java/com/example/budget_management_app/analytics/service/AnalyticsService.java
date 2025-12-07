package com.example.budget_management_app.analytics.service;

import com.example.budget_management_app.analytics.dto.*;
import com.example.budget_management_app.transaction_common.domain.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsService {

    List<BalanceHistoryPointDto> getAccountBalanceHistory(Long userId, Long accountId, LocalDateTime from, LocalDateTime to);

    List<CategoryBreakdownPointDto> getAccountCategoryBreakdown(Long userId, Long accountId, LocalDateTime from, LocalDateTime to, TransactionType type);

    List<CashFlowPointDto> getAccountCashFlow(Long userId, Long accountId, LocalDateTime from, LocalDateTime to);

    FinancialSummaryDto getGlobalFinancialSummary(Long userId, LocalDateTime from, LocalDateTime to);

    List<CategoryBreakdownPointDto> getGlobalCategoryBreakdown (Long userId, LocalDateTime from, LocalDateTime to, TransactionType type);

    List<CashFlowPointDto> getGlobalCashFlow(Long userId, LocalDateTime from, LocalDateTime to);

    MultiSeriesChartDto getGlobalBalanceHistory(Long userId, LocalDateTime from, LocalDateTime to);
}
