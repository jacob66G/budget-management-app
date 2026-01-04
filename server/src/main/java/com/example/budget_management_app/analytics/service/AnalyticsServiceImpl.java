package com.example.budget_management_app.analytics.service;

import com.example.budget_management_app.account.dao.AccountDao;
import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.analytics.dao.AnalyticsDao;
import com.example.budget_management_app.analytics.dto.*;
import com.example.budget_management_app.analytics.mapper.Mapper;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.transaction.dao.TransactionDao;
import com.example.budget_management_app.transaction.domain.SortDirection;
import com.example.budget_management_app.transaction.domain.SortedBy;
import com.example.budget_management_app.transaction.domain.TransactionModeFilter;
import com.example.budget_management_app.transaction.dto.TransactionFilterParams;
import com.example.budget_management_app.transaction.dto.TransactionPaginationParams;
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
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AccountDao accountDao;
    private final TransactionDao transactionDao;
    private final AnalyticsDao analyticsDao;
    private final Mapper mapper;

    @Override
    public List<BalanceHistoryPointDto> getAccountBalanceHistory(Long userId, Long accountId, LocalDateTime from, LocalDateTime to) {
        Account account = accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account with id: " + accountId + "  not found.", ErrorCode.NOT_FOUND));

        LocalDateTime effectiveTo = getEffectiveTo(to);

        if (from.isAfter(effectiveTo)) {
            return new ArrayList<>();
        }

        BigDecimal currentBalance = account.getBalance();

        BigDecimal changesAfterPeriod = analyticsDao.sumAccountNetChangeAfterDate(accountId, effectiveTo);
        BigDecimal balanceAtEnd = currentBalance.subtract(changesAfterPeriod);

        long daysDiff = ChronoUnit.DAYS.between(from, effectiveTo);

        if (daysDiff > 90) {
            return getMonthlyHistory(accountId, from, effectiveTo, balanceAtEnd);
        } else {
            return getDailyHistory(accountId, from, effectiveTo, balanceAtEnd);
        }
    }

    @Override
    public List<CategoryBreakdownPointDto> getAccountCategoryBreakdown(Long userId, Long accountId, LocalDateTime from, LocalDateTime to, TransactionType type) {
        accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account with id: " + accountId + "  not found.", ErrorCode.NOT_FOUND));

        LocalDateTime effectiveTo = getEffectiveTo(to);

        if (from.isAfter(effectiveTo)) {
            return new ArrayList<>();
        }
        return analyticsDao.getAccountCategoryBreakdown(accountId, from, effectiveTo, type);
    }

    @Override
    public List<CashFlowPointDto> getAccountCashFlow(Long userId, Long accountId, LocalDateTime from, LocalDateTime to) {
        accountDao.findByIdAndUser(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account with id: " + accountId + "  not found.", ErrorCode.NOT_FOUND));

        LocalDateTime effectiveTo = getEffectiveTo(to);

        if (from.isAfter(effectiveTo)) {
            return new ArrayList<>();
        }

        long daysDiff = ChronoUnit.DAYS.between(from, effectiveTo);

        if (daysDiff <= 90) {
            List<CashFlowPointDto> rawData = analyticsDao.getAccountDailyCashFlow(accountId, from, effectiveTo);
            return getDailyCashFlowFilled(rawData, from, effectiveTo);
        } else {
            List<PeriodSumCashFlowDto> rawData = analyticsDao.getAccountMonthlyCashFlow(accountId, from, effectiveTo);
            return getMonthlyCashFlowFilled(rawData, from, effectiveTo);
        }
    }

    @Override
    public MultiSeriesChartDto getGlobalBalanceHistory(Long userId, LocalDateTime from, LocalDateTime to) {
        List<LocalDate> masterTimeline = generateMasterTimeline(from.toLocalDate(), to.toLocalDate());

        List<LocalDate> labels = masterTimeline.stream().toList();

        List<ChartSeriesDto> seriesList = new ArrayList<>();

        List<Account> accounts = accountDao.findByUser(userId).stream()
                .filter(Account::isIncludeInTotalBalance)
                .toList();

        for (Account acc : accounts) {
            List<BalanceHistoryPointDto> rawHistory = getAccountBalanceHistory(userId, acc.getId(), from, to);

            Map<LocalDate, BigDecimal> historyMap = rawHistory.stream()
                    .collect(Collectors.toMap(BalanceHistoryPointDto::date, BalanceHistoryPointDto::amount));

            List<BigDecimal> alignedData = new ArrayList<>();

            for (LocalDate datePoint : masterTimeline) {
                alignedData.add(historyMap.getOrDefault(datePoint, BigDecimal.ZERO));
            }

            seriesList.add(new ChartSeriesDto(acc.getName(), alignedData));
        }

        return new MultiSeriesChartDto(labels, seriesList);
    }

    @Override
    public BigDecimal getNetChangeAfterDate(Long accountId, LocalDateTime date) {
        return analyticsDao.sumAccountNetChangeAfterDate(accountId, date);
    }

    @Override
    public BigDecimal getTotalByType(Long accountId, LocalDateTime from, LocalDateTime to, TransactionType type) {
        return analyticsDao.sumAccountTotalByType(accountId, from, to, type);
    }

    private List<LocalDate> generateMasterTimeline(LocalDate from, LocalDate to) {
        List<LocalDate> timeline = new ArrayList<>();
        long daysDiff = ChronoUnit.DAYS.between(from, to);
        boolean isMonthly = daysDiff > 90;

        if (isMonthly) {
            YearMonth startMonth = YearMonth.from(from);
            YearMonth endMonth = YearMonth.from(to);

            for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
                LocalDate pointDate = month.atEndOfMonth();
                if (month.equals(endMonth) && pointDate.isAfter(to)) {
                    pointDate = to;
                }
                timeline.add(pointDate);
            }
        } else {
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                timeline.add(date);
            }
        }
        return timeline;
    }

    @Override
    public List<CategoryBreakdownPointDto> getGlobalCategoryBreakdown(Long userId, LocalDateTime from, LocalDateTime to, TransactionType type) {
        return analyticsDao.getGlobalCategoryBreakdown(userId, from, to, type);
    }

    @Override
    public List<CashFlowPointDto> getGlobalCashFlow(Long userId, LocalDateTime from, LocalDateTime to) {
        long daysDiff = ChronoUnit.DAYS.between(from, to);

        if (daysDiff <= 90) {
            List<CashFlowPointDto> rawData = analyticsDao.getGlobalDailyCashFlow(userId, from, to);
            return getDailyCashFlowFilled(rawData, from, to);
        } else {
            List<PeriodSumCashFlowDto> rawData = analyticsDao.getGlobalMonthlyCashFlow(userId, from, to);
            return getMonthlyCashFlowFilled(rawData, from, to);
        }
    }

    @Override
    public FinancialSummaryDto getGlobalFinancialSummary(Long userId, LocalDateTime from, LocalDateTime to) {
        List<Account> allAccounts = accountDao.findByUser(userId);

        List<Account> analyticAccounts = allAccounts.stream()
                .filter(Account::isIncludeInTotalBalance)
                .toList();

        BigDecimal currentTotalBalance = analyticAccounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal changesAfterPeriod = analyticsDao.sumGlobalNetChangeAfterDate(userId, to);
        BigDecimal closingBalance = currentTotalBalance.subtract(changesAfterPeriod);

        BigDecimal incomeInRange = analyticsDao.sumGlobalTotalByType(userId, from, to, TransactionType.INCOME);
        BigDecimal expenseInRange = analyticsDao.sumGlobalTotalByType(userId, from, to, TransactionType.EXPENSE);
        BigDecimal netSavings = incomeInRange.subtract(expenseInRange);

        List<Long> analyticAccountIds = analyticAccounts.stream().map(Account::getId).toList();

        List<TransactionSummaryDto> transactionsSummary = List.of();
        if (!analyticAccountIds.isEmpty()) {
            TransactionPaginationParams pagination = new TransactionPaginationParams(1, 10, SortedBy.DATE, SortDirection.DESC);
            TransactionFilterParams filters = new TransactionFilterParams();
            filters.setMode(TransactionModeFilter.REGULAR);
            filters.setAccountIds(analyticAccountIds);

            transactionsSummary = transactionDao.getTuples(pagination, filters, userId)
                    .stream().map(mapper::toTransactionSummary).toList();
        }

        List<AccountSummaryDto> accountsSummary = allAccounts.stream()
                .map(mapper::toAccountSummary)
                .toList();

        return new FinancialSummaryDto(
                closingBalance,
                incomeInRange,
                expenseInRange,
                netSavings,
                accountsSummary,
                transactionsSummary
        );
    }

    private List<CashFlowPointDto> getDailyCashFlowFilled(List<CashFlowPointDto> rawData, LocalDateTime from, LocalDateTime to) {
        Map<LocalDate, CashFlowPointDto> dataMap = rawData.stream()
                .collect(Collectors.toMap(CashFlowPointDto::date, dto -> dto));

        List<CashFlowPointDto> result = new ArrayList<>();

        for (LocalDate date = from.toLocalDate(); !date.isAfter(to.toLocalDate()); date = date.plusDays(1)) {
            if (dataMap.containsKey(date)) {
                result.add(dataMap.get(date));
            } else {
                result.add(new CashFlowPointDto(date, BigDecimal.ZERO, BigDecimal.ZERO));
            }
        }
        return result;
    }

    private List<CashFlowPointDto> getMonthlyCashFlowFilled(List<PeriodSumCashFlowDto> rawData, LocalDateTime from, LocalDateTime to) {
        Map<YearMonth, PeriodSumCashFlowDto> dataMap = rawData.stream()
                .collect(Collectors.toMap(
                        dto -> YearMonth.of(dto.year(), dto.month()),
                        dto -> dto
                ));

        List<CashFlowPointDto> result = new ArrayList<>();

        YearMonth startMonth = YearMonth.from(from);
        YearMonth endMonth = YearMonth.from(to);

        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            LocalDate periodStart = month.atDay(1);

            if (dataMap.containsKey(month)) {
                PeriodSumCashFlowDto dto = dataMap.get(month);
                result.add(new CashFlowPointDto(
                        periodStart,
                        dto.totalIncome(),
                        dto.totalExpense()
                ));
            } else {
                result.add(new CashFlowPointDto(
                        periodStart,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));
            }
        }

        return result;
    }

    private List<BalanceHistoryPointDto> getDailyHistory(Long accountId, LocalDateTime from, LocalDateTime to, BigDecimal balanceAtEnd) {
        List<DailySumDto> dailyChanges = analyticsDao.getAccountDailyNetChanges(accountId, from, to);

        Map<LocalDate, BigDecimal> changesMap = dailyChanges.stream()
                .collect(Collectors.toMap(DailySumDto::date, DailySumDto::amount));

        List<BalanceHistoryPointDto> result = new ArrayList<>();

        LocalDate endDate = to.toLocalDate();
        LocalDate startDate = from.toLocalDate();

        BigDecimal runningBalance = balanceAtEnd;
        for (LocalDate date = endDate; !date.isBefore(startDate); date = date.minusDays(1)) {
            result.addFirst(new BalanceHistoryPointDto(date, runningBalance));

            BigDecimal changeOnThisDay = changesMap.getOrDefault(date, BigDecimal.ZERO);
            runningBalance = runningBalance.subtract(changeOnThisDay);
        }

        return result;
    }

    private List<BalanceHistoryPointDto> getMonthlyHistory(Long accountId, LocalDateTime from, LocalDateTime to, BigDecimal balanceAtEnd) {
        List<PeriodSumDto> monthlyChanges = analyticsDao.getAccountMonthlyNetChanges(accountId, from, to);

        Map<YearMonth, BigDecimal> changesMap = monthlyChanges.stream()
                .collect(Collectors.toMap(
                        dto -> YearMonth.of(dto.year(), dto.month()),
                        PeriodSumDto::amount
                ));

        List<BalanceHistoryPointDto> result = new ArrayList<>();

        YearMonth startMonth = YearMonth.from(from);
        YearMonth endMonth = YearMonth.from(to);

        BigDecimal runningBalance = balanceAtEnd;

        for (YearMonth month = endMonth; !month.isBefore(startMonth); month = month.minusMonths(1)) {

            LocalDate pointDate = month.atEndOfMonth();

            if (month.equals(endMonth) && pointDate.isAfter(to.toLocalDate())) {
                pointDate = to.toLocalDate();
            }

            result.addFirst(new BalanceHistoryPointDto(pointDate, runningBalance));

            BigDecimal changeInMonth = changesMap.getOrDefault(month, BigDecimal.ZERO);
            runningBalance = runningBalance.subtract(changeInMonth);
        }

        return result;
    }

    private LocalDateTime getEffectiveTo(LocalDateTime to) {
        LocalDateTime now = LocalDateTime.now();
        if (to.isAfter(now)) {
            return now;
        }
        return to;
    }
}
