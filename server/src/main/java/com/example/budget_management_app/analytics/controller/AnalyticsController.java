package com.example.budget_management_app.analytics.controller;

import com.example.budget_management_app.analytics.dto.CashFlowChartPointDto;
import com.example.budget_management_app.analytics.dto.DateRange;
import com.example.budget_management_app.analytics.dto.CategoryChartPoint;
import com.example.budget_management_app.analytics.dto.ChartPointDto;
import com.example.budget_management_app.analytics.service.AnalyticsService;
import com.example.budget_management_app.security.service.CustomUserDetails;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/balance-history/{accountId}")
    public ResponseEntity<List<ChartPointDto>> getBalanceHistory(
            @PathVariable Long accountId,
            @Valid DateRange dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(analyticsService.getBalanceHistory(userDetails.getId(), accountId, dto.getFromDateTime(), dto.getToDateTime()));
    }

    @GetMapping("/categories/{accountId}")
    public ResponseEntity<List<CategoryChartPoint>> getCategoryBreakdown(
            @PathVariable Long accountId,
            @RequestParam TransactionType type,
            @Valid DateRange dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(analyticsService.getCategoryBreakdown(userDetails.getId(), accountId, dto.getFromDateTime(), dto.getToDateTime(), type));
    }

    @GetMapping("/cash-flow/{accountId}")
    public ResponseEntity<List<CashFlowChartPointDto>> getCashFlow(
            @PathVariable Long accountId,
            @Valid DateRange dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(analyticsService.getCashFlow(userDetails.getId(), accountId, dto.getFromDateTime(), dto.getToDateTime()));
    }
}
