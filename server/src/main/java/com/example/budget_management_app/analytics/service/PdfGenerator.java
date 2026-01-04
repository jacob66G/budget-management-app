package com.example.budget_management_app.analytics.service;

import com.example.budget_management_app.analytics.dto.FinancialReportData;
import com.example.budget_management_app.analytics.dto.ReportCharts;

public interface PdfGenerator {

    byte[] generateFinancialReportPdf(FinancialReportData data, ReportCharts chartsData);
}
