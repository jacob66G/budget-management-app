package com.example.budget_management_app.analytics.service;

import com.example.budget_management_app.analytics.dto.BalanceHistoryPointDto;
import com.example.budget_management_app.analytics.dto.CashFlowPointDto;
import com.example.budget_management_app.analytics.dto.CategoryBreakdownPointDto;

import java.util.List;

public interface ChartService {
    byte[] generateBalanceChart(List<BalanceHistoryPointDto> data);
    byte[] generateCashFlowChart(List<CashFlowPointDto> data);
    byte[] generateCategorySumChart(List<CategoryBreakdownPointDto> data);
}
