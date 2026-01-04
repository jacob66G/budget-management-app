package com.example.budget_management_app.analytics.service;

import com.example.budget_management_app.analytics.dto.FinancialReportData;
import com.example.budget_management_app.analytics.dto.ReportCharts;
import com.example.budget_management_app.common.exception.InternalException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfGeneratorImpl implements PdfGenerator {

    private final TemplateEngine templateEngine;

    @Override
    public byte[] generateFinancialReportPdf(FinancialReportData data, ReportCharts chartsData) {
        Context context = new Context();
        context.setVariables(prepareVariables(data, chartsData));

        String htmlContent = templateEngine.process("financial-report", context);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new InternalException("Unexpected error during generating PDF", e);
        }
    }

    private Map<String, Object> prepareVariables(FinancialReportData data, ReportCharts chartsData) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("accountName", data.accountName());
        variables.put("fromDate", data.from());
        variables.put("toDate", data.to());
        variables.put("generatedAt", data.generatedAt());
        variables.put("currency", data.currency());
        variables.put("closingBalance", data.closingBalance());
        variables.put("totalIncome", data.totalIncome());
        variables.put("totalExpense", data.totalExpense());
        variables.put("balanceInPeriod", data.balanceInPeriod());
        variables.put("savingsRatio", data.savingsRatio());
        variables.put("averageDailyExpenses", data.averageDailyExpenses());
        variables.put("balanceHistoryChartData", toBase64(chartsData.balanceHistoryChart()));
        variables.put("expenseCategoryChartData", toBase64(chartsData.expenseCategoryChart()));
        variables.put("incomeCategoryChartData", toBase64(chartsData.incomeCategoryChart()));
        variables.put("cashFlowChartData", toBase64(chartsData.cashFlowChart()));
        variables.put("expenseList", data.expenseBreakdown());
        variables.put("incomeList", data.incomeBreakdown());
        return variables;
    }

    private String toBase64(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
