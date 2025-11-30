package com.example.budget_management_app.analytics.service;

import com.example.budget_management_app.analytics.dto.CashFlowChartPointDto;
import com.example.budget_management_app.analytics.dto.CategoryChartPoint;
import com.example.budget_management_app.analytics.dto.ChartPointDto;
import com.example.budget_management_app.transaction_common.domain.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsService {

    List<ChartPointDto> getBalanceHistory(Long userId, Long accountId, LocalDateTime from, LocalDateTime to);

    List<CategoryChartPoint> getCategoryBreakdown(Long userId, Long accountId, LocalDateTime from, LocalDateTime to, TransactionType type);

    List<CashFlowChartPointDto> getCashFlow(Long userId, Long accountId, LocalDateTime from, LocalDateTime to);
}
