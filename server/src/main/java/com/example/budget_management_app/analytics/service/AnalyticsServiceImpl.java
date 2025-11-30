package com.example.budget_management_app.analytics.service;

import com.example.budget_management_app.account.dao.AccountDao;
import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.analytics.dao.AnalyticsDao;
import com.example.budget_management_app.analytics.dto.*;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AccountDao accountDao;
    private final AnalyticsDao analyticsDao;

    @Override
    @Transactional(readOnly = true)
    public List<ChartPointDto> getBalanceHistory(Long userId, Long accountId, LocalDateTime from, LocalDateTime to) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account with id: " + accountId + "  not found.", ErrorCode.NOT_FOUND));

        BigDecimal currentBalance = account.getBalance();

        BigDecimal changesAfterPeriod = analyticsDao.sumAmountAfterDate(accountId, to);
        BigDecimal balanceAtEnd = currentBalance.subtract(changesAfterPeriod);

        long daysDiff = ChronoUnit.DAYS.between(from, to);

        if (daysDiff > 90) {
            return getMonthlyHistory(accountId, from, to, balanceAtEnd);
        } else {
            return getDailyHistory(accountId, from, to, balanceAtEnd);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryChartPoint> getCategoryBreakdown(Long userId, Long accountId, LocalDateTime from, LocalDateTime to, TransactionType type) {
        accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account with id: " + accountId + "  not found.", ErrorCode.NOT_FOUND));

        return analyticsDao.getCategorySums(accountId, from, to, type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashFlowChartPointDto> getCashFlow(Long userId, Long accountId, LocalDateTime from, LocalDateTime to) {
        accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account with id: " + accountId + "  not found.", ErrorCode.NOT_FOUND));

        long daysDiff = ChronoUnit.DAYS.between(from, to);

        if (daysDiff <= 90) {
            return getDailyCashFlowFilled(accountId, from, to);
        } else {
            return getMonthlyCashFlowFilled(accountId, from, to);
        }
    }

    private List<CashFlowChartPointDto> getMonthlyCashFlowFilled(Long accountId, LocalDateTime from, LocalDateTime to) {
        List<PeriodSumCashFlowDto> rawData = analyticsDao.getMonthlyCashFlow(accountId, from, to);

        Map<YearMonth, PeriodSumCashFlowDto> dataMap = rawData.stream()
                .collect(Collectors.toMap(
                        dto -> YearMonth.of(dto.year(), dto.month()),
                        dto -> dto
                ));

        List<CashFlowChartPointDto> result = new ArrayList<>();

        YearMonth startMonth = YearMonth.from(from);
        YearMonth endMonth = YearMonth.from(to);

        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            LocalDate periodStart = month.atDay(1);

            if (dataMap.containsKey(month)) {
                PeriodSumCashFlowDto dto = dataMap.get(month);
                result.add(new CashFlowChartPointDto(
                        periodStart,
                        dto.totalIncome(),
                        dto.totalExpense()
                ));
            } else {
                result.add(new CashFlowChartPointDto(
                        periodStart,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));
            }
        }

        return result;
    }

    private List<CashFlowChartPointDto> getDailyCashFlowFilled(Long accountId, LocalDateTime from, LocalDateTime to) {
        List<CashFlowChartPointDto> rawData = analyticsDao.getDailyCashFlow(accountId, from, to);

        Map<LocalDate, CashFlowChartPointDto> dataMap = rawData.stream()
                .collect(Collectors.toMap(CashFlowChartPointDto::date, dto -> dto));

        List<CashFlowChartPointDto> result = new ArrayList<>();

        for (LocalDate date = from.toLocalDate(); !date.isAfter(to.toLocalDate()); date = date.plusDays(1)) {
            if (dataMap.containsKey(date)) {
                result.add(dataMap.get(date));
            } else {
                result.add(new CashFlowChartPointDto(date, BigDecimal.ZERO, BigDecimal.ZERO));
            }
        }
        return result;
    }

    private List<ChartPointDto> getMonthlyHistory(Long accountId, LocalDateTime from, LocalDateTime to, BigDecimal balanceAtEnd) {
        List<PeriodSumDto> monthlyChanges = analyticsDao.getMonthlySums(accountId, from, to);

        Map<YearMonth, BigDecimal> changesMap = monthlyChanges.stream()
                .collect(Collectors.toMap(
                        dto -> YearMonth.of(dto.year(), dto.month()),
                        PeriodSumDto::amount
                ));

        List<ChartPointDto> result = new ArrayList<>();

        YearMonth startMonth = YearMonth.from(from);
        YearMonth endMonth = YearMonth.from(to);

        BigDecimal runningBalance = balanceAtEnd;

        for (YearMonth month = endMonth; !month.isBefore(startMonth); month = month.minusMonths(1)) {

            LocalDate pointDate = month.atEndOfMonth();

            if (month.equals(endMonth) && pointDate.isAfter(to.toLocalDate())) {
                pointDate = to.toLocalDate();
            }

            result.addFirst(new ChartPointDto(pointDate, runningBalance));

            BigDecimal changeInMonth = changesMap.getOrDefault(month, BigDecimal.ZERO);
            runningBalance = runningBalance.subtract(changeInMonth);
        }

        return result;
    }

    private List<ChartPointDto> getDailyHistory(Long accountId, LocalDateTime from, LocalDateTime to, BigDecimal balanceAtEnd) {
        List<DailySumDto> dailyChanges = analyticsDao.getDailySums(accountId, from, to);

        Map<LocalDate, BigDecimal> changesMap = dailyChanges.stream()
                .collect(Collectors.toMap(DailySumDto::date, DailySumDto::amount));

        List<ChartPointDto> result = new ArrayList<>();

        LocalDate endDate = to.toLocalDate();
        LocalDate startDate = from.toLocalDate();

        BigDecimal runningBalance = balanceAtEnd;
        for (LocalDate date = endDate; !date.isBefore(startDate); date = date.minusDays(1)) {
            result.addFirst(new ChartPointDto(date, runningBalance));

            BigDecimal changeOnThisDay = changesMap.getOrDefault(date, BigDecimal.ZERO);
            runningBalance = runningBalance.subtract(changeOnThisDay);
        }

        return result;
    }
}
