package com.example.budget_management_app.analytics.service;

import com.example.budget_management_app.account.dao.AccountDao;
import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.analytics.dto.CategoryBreakdownPointDto;
import com.example.budget_management_app.analytics.dto.FinancialReportData;
import com.example.budget_management_app.analytics.dto.ReportCharts;
import com.example.budget_management_app.analytics.events.FinancialReportEvent;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialReportServiceImpl implements FinancialReportService {

    private final AnalyticsService analyticsService;
    private final AccountDao accountDao;
    private final ChartService chartService;
    private final PdfGenerator pdfGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Override
    public void generateFinancialReport(Long userId, Long accountId, LocalDateTime from, LocalDateTime to) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account with id: " + accountId + "  not found.", ErrorCode.NOT_FOUND));

        User user = account.getUser();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime effectiveTo = to.isAfter(now) ? now : to;

        List<CategoryBreakdownPointDto> expenseByCategory = analyticsService.getAccountCategoryBreakdown(userId, accountId, from, effectiveTo, TransactionType.EXPENSE);
        List<CategoryBreakdownPointDto> incomeByCategory = analyticsService.getAccountCategoryBreakdown(userId, accountId, from, effectiveTo, TransactionType.INCOME);

        FinancialReportData data = gatherFinancialData(account, from, effectiveTo, now, expenseByCategory, incomeByCategory);
        ReportCharts charts = generateCharts(userId, accountId, from, effectiveTo, expenseByCategory, incomeByCategory);

        byte[] pdfData = pdfGenerator.generateFinancialReportPdf(data, charts);

        eventPublisher.publishEvent(new FinancialReportEvent(user.getEmail(), user.getName(), pdfData));
    }

    private FinancialReportData gatherFinancialData(
            Account account, LocalDateTime from,
            LocalDateTime effectiveTo, LocalDateTime now,
            List<CategoryBreakdownPointDto> expenseByCategory,
            List<CategoryBreakdownPointDto> incomeByCategory
    ) {
        BigDecimal currentBalance = account.getBalance();
        BigDecimal changesAfterPeriod = analyticsService.getNetChangeAfterDate(account.getId(), effectiveTo);
        BigDecimal balanceAtEnd = currentBalance.subtract(changesAfterPeriod);

        BigDecimal totalIncome = analyticsService.getTotalByType(account.getId(), from, effectiveTo, TransactionType.INCOME);
        BigDecimal totalExpense = analyticsService.getTotalByType(account.getId(), from, effectiveTo, TransactionType.EXPENSE);

        String currency = account.getCurrency().name();

        BigDecimal balanceInPeriod = totalIncome.subtract(totalExpense);
        BigDecimal savingRatio = calculateSavingsRatio(balanceInPeriod, totalIncome);
        BigDecimal averageDailyExpenses = calculateDailyAverage(totalExpense, from, effectiveTo);


        return FinancialReportData.builder()
                .accountName(account.getName())
                .from(from)
                .to(effectiveTo)
                .generatedAt(now)
                .currency(currency)
                .closingBalance(balanceAtEnd)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balanceInPeriod(balanceInPeriod)
                .savingsRatio(savingRatio.doubleValue())
                .averageDailyExpenses(averageDailyExpenses)
                .expenseBreakdown(expenseByCategory)
                .incomeBreakdown(incomeByCategory)
                .build();
    }

    private BigDecimal calculateDailyAverage(BigDecimal totalExpenses, LocalDateTime from, LocalDateTime to) {
        long daysInPeriod = ChronoUnit.DAYS.between(from, to);

        if (daysInPeriod == 0) {
            return totalExpenses;
        }
        return totalExpenses.divide(BigDecimal.valueOf(daysInPeriod), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSavingsRatio(BigDecimal balance, BigDecimal income) {
        if (income.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return balance.divide(income, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private ReportCharts generateCharts(
            Long userId, Long accountId,
            LocalDateTime from, LocalDateTime effectiveTo,
            List<CategoryBreakdownPointDto> expenseByCategory,
            List<CategoryBreakdownPointDto> incomeByCategory
    ) {
        var balanceHistoryData = analyticsService.getAccountBalanceHistory(userId, accountId, from, effectiveTo);
        var cashFlowData = analyticsService.getAccountCashFlow(userId, accountId, from, effectiveTo);

        byte[] historyChart = chartService.generateBalanceChart(balanceHistoryData);
        byte[] expenseChart = chartService.generateCategorySumChart(expenseByCategory);
        byte[] incomeChart = chartService.generateCategorySumChart(incomeByCategory);
        byte[] cashFlowChart = chartService.generateCashFlowChart(cashFlowData);

        return new ReportCharts(historyChart, expenseChart, incomeChart, cashFlowChart);
    }
}
