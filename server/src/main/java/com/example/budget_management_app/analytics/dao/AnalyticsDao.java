package com.example.budget_management_app.analytics.dao;

import com.example.budget_management_app.analytics.dto.*;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AnalyticsDao {

    @PersistenceContext
    private final EntityManager em;

    public BigDecimal sumAmountAfterDate(Long accountId, LocalDateTime cutoffDate) {
        String query = "SELECT COALESCE(SUM(CASE WHEN t.type  = 'EXPENSE' THEN -t.amount ELSE t.amount END), 0) FROM Transaction t WHERE t.account.id = :accountId AND t.transactionDate > :cutoffDate";

        return em.createQuery(query, BigDecimal.class)
                .setParameter("accountId", accountId)
                .setParameter("cutoffDate", cutoffDate)
                .getSingleResult();
    }

    public List<CategoryChartPoint> getCategorySums(Long accountId, LocalDateTime startDate, LocalDateTime endDate, TransactionType transactionType) {
        String query = "SELECT t.category.name, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
                "WHERE t.account.id = :accountId AND t.type = :transactionType " +
                "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
                "GROUP BY t.category.name " +
                "ORDER BY SUM(t.amount)";

        return em.createQuery(query, CategoryChartPoint.class)
                .setParameter("accountId", accountId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("transactionType", transactionType)
                .getResultList();
    }

    public List<CashFlowChartPointDto> getDailyCashFlow(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        String query = "SELECT CAST(t.transactionDate as LocalDate), SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) " +
                "FROM Transaction t WHERE t.account.id = :accountId AND t.transactionDate BETWEEN :startDate AND :endDate " +
                "GROUP BY CAST(t.transactionDate AS LocalDate) " +
                "ORDER BY CAST(t.transactionDate AS LocalDate) ASC";

        return em.createQuery(query, CashFlowChartPointDto.class)
                .setParameter("accountId", accountId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<PeriodSumCashFlowDto> getMonthlyCashFlow(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        String query = "SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) " +
                "FROM Transaction t WHERE t.account.id = :accountId AND t.transactionDate BETWEEN :startDate AND :endDate " +
                "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
                "ORDER BY YEAR(t.transactionDate) DESC, MONTH(t.transactionDate) ASC";

        return em.createQuery(query, PeriodSumCashFlowDto.class)
                .setParameter("accountId", accountId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<DailySumDto> getDailySums(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return em.createQuery("SELECT CAST(t.transactionDate as LocalDate), SUM(CASE WHEN t.type = 'EXPENSE' THEN -t.amount ELSE t.amount END) " +
                        "FROM Transaction t " +
                        "WHERE t.account.id = :accountId AND t.transactionDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY CAST(t.transactionDate as LocalDate) " +
                        "ORDER BY CAST(t.transactionDate as LocalDate) DESC", DailySumDto.class)
                .setParameter("accountId", accountId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<PeriodSumDto> getMonthlySums(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return em.createQuery("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), SUM(CASE WHEN t.type = 'EXPENSE' THEN -t.amount ELSE t.amount END) " +
                        "FROM Transaction t " +
                        "WHERE t.account.id = :accountId AND t.transactionDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
                        "ORDER BY YEAR(t.transactionDate) DESC, MONTH(t.transactionDate) DESC", PeriodSumDto.class)
                .setParameter("accountId", accountId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<PeriodSumDto> getYearlySums(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return em.createQuery("SELECT YEAR(t.transactionDate), SUM(CASE WHEN t.type = 'EXPENSE' THEN -t.amount ELSE t.amount END) " +
                        "FROM Transaction t " +
                        "WHERE t.account.id = :accountId AND t.transactionDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY YEAR (t.transactionDate) " +
                        "ORDER BY YEAR(t.transactionDate) DESC", PeriodSumDto.class)
                .setParameter("accountId", accountId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

}
