package com.example.budget_management_app.analytics.service;

import java.time.LocalDateTime;

public interface FinancialReportService {

    void generateFinancialReport(Long userId, Long accountId, LocalDateTime from, LocalDateTime to);
}
