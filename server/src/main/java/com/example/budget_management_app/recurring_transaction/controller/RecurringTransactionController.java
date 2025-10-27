package com.example.budget_management_app.recurring_transaction.controller;

import com.example.budget_management_app.recurring_transaction.dto.RecurringTransactionSummary;
import com.example.budget_management_app.recurring_transaction.service.RecurringTransactionService;
import com.example.budget_management_app.security.service.CustomUserDetails;
import com.example.budget_management_app.transaction.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/recurring-transactions", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @GetMapping
    public ResponseEntity<PagedResponse<RecurringTransactionSummary>> getSummaries(
            @RequestParam(name = "page") int page,
            @RequestParam(name = "limit") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        return ResponseEntity.ok(recurringTransactionService.getSummaries(
                userDetails.getId(),
                page,
                limit));
    }
}
