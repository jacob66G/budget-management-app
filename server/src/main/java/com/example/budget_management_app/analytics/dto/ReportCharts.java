package com.example.budget_management_app.analytics.dto;

public record ReportCharts(
        byte[] balanceHistoryChart,
        byte[] expenseCategoryChart,
        byte[] incomeCategoryChart,
        byte[] cashFlowChart
) {
}
