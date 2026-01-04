package com.example.budget_management_app.analytics.events;

public record FinancialReportEvent(
        String userEmail,
        String userName,
        byte[] pdfData
) {
}
