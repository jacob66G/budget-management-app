package com.example.budget_management_app.analytics.controller;

import com.example.budget_management_app.analytics.dto.*;
import com.example.budget_management_app.analytics.service.AnalyticsService;
import com.example.budget_management_app.analytics.service.FinancialReportService;
import com.example.budget_management_app.security.service.CustomUserDetails;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final FinancialReportService financialReportService;

    @GetMapping("/accounts/{accountId}/balance-history")
    public ResponseEntity<List<BalanceHistoryPointDto>> getAccountBalanceHistory(
            @PathVariable Long accountId,
            @Valid DateRange dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(analyticsService.getAccountBalanceHistory(userDetails.getId(), accountId, dto.getFromDateTime(), dto.getToDateTime()));
    }

    @GetMapping("/accounts/{accountId}/categories")
    public ResponseEntity<List<CategoryBreakdownPointDto>> getAccountCategoryBreakdown(
            @PathVariable Long accountId,
            @RequestParam TransactionType type,
            @Valid DateRange dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(analyticsService.getAccountCategoryBreakdown(userDetails.getId(), accountId, dto.getFromDateTime(), dto.getToDateTime(), type));
    }

    @GetMapping("/accounts/{accountId}/cash-flow")
    public ResponseEntity<List<CashFlowPointDto>> getAccountCashFlow(
            @PathVariable Long accountId,
            @Valid DateRange dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(analyticsService.getAccountCashFlow(userDetails.getId(), accountId, dto.getFromDateTime(), dto.getToDateTime()));
    }

    @GetMapping("/global/summary")
    public ResponseEntity<FinancialSummaryDto> getGlobalFinancialSummary(@Valid DateRange dto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getGlobalFinancialSummary(userDetails.getId(), dto.getFromDateTime(), dto.getToDateTime()));
    }

    @GetMapping("/global/balance-history")
    public ResponseEntity<MultiSeriesChartDto> getGlobalBalanceHistory(
            @Valid DateRange dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(analyticsService.getGlobalBalanceHistory(userDetails.getId(), dto.getFromDateTime(), dto.getToDateTime()));
    }

    @GetMapping("/global/categories")
    public ResponseEntity<List<CategoryBreakdownPointDto>> getGlobalCategoryBreakdown(
            @RequestParam TransactionType type,
            @Valid DateRange dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(analyticsService.getGlobalCategoryBreakdown(userDetails.getId(), dto.getFromDateTime(), dto.getToDateTime(), type));
    }

    @GetMapping("/global/cash-flow")
    public ResponseEntity<List<CashFlowPointDto>> getGlobalCashFlow(
            @Valid DateRange dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(analyticsService.getGlobalCashFlow(userDetails.getId(), dto.getFromDateTime(), dto.getToDateTime()));
    }

    @GetMapping("/accounts/{accountId}/generate-report")
    public ResponseEntity<Void> generateReport(
            @PathVariable Long accountId,
            @Valid DateRange dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        financialReportService.generateFinancialReport(userDetails.getId(), accountId, dto.getFromDateTime(), dto.getToDateTime());
        return ResponseEntity.ok().build();
    }
}
