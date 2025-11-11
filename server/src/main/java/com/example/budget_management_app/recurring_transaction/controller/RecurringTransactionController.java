package com.example.budget_management_app.recurring_transaction.controller;

import com.example.budget_management_app.common.utils.PaginationUtils;
import com.example.budget_management_app.recurring_transaction.domain.RemovalRange;
import com.example.budget_management_app.recurring_transaction.domain.UpdateRange;
import com.example.budget_management_app.recurring_transaction.dto.*;
import com.example.budget_management_app.recurring_transaction.service.RecurringTransactionService;
import com.example.budget_management_app.security.service.CustomUserDetails;
import com.example.budget_management_app.transaction_common.dto.PageRequest;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(path = "/api/v1/recurring-transactions", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @GetMapping
    public ResponseEntity<PagedResponse<RecurringTransactionSummary>> getSummariesPage(
            PageRequest pageReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        PagedResponse<RecurringTransactionSummary> summariesPage = recurringTransactionService.getSummariesPage(
                pageReq,
                userDetails.getId());

        return ResponseEntity.ok(summariesPage.withLinks(PaginationUtils.createLinks(summariesPage.pagination())));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<RecurringTransactionDetailsResponse> getDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(recurringTransactionService.getDetails(
                id,
                userDetails.getId()
        ));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<PagedResponse<UpcomingTransactionSummary>> getUpcomingSummaries(
            PageRequest pageReq,
            UpcomingTransactionSearchCriteria searchCriteria,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        PagedResponse<UpcomingTransactionSummary> upcomingTransactionsPage = this.recurringTransactionService.getUpcomingTransactionsPage(
                pageReq,
                searchCriteria,
                userDetails.getId()
        );

        return ResponseEntity.ok(upcomingTransactionsPage.withLinks(PaginationUtils.createLinks(upcomingTransactionsPage.pagination())));
    }

    @PostMapping
    public ResponseEntity<RecurringTransactionCreateResponse> create(
            @RequestBody RecurringTransactionCreateRequest createReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        RecurringTransactionCreateResponse response = recurringTransactionService.create(createReq, userDetails.getId());

        URI location = URI.create("/api/v1/recurring-transactions/" + response.id());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long id,
            @RequestBody RecurringTransactionStatusUpdateRequest updateReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        recurringTransactionService.changeStatus(id, updateReq.isActive(), userDetails.getId());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestParam(name = "range") UpdateRange range,
            @RequestBody RecurringTransactionUpdateRequest updateReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        recurringTransactionService.update(id, updateReq, range, userDetails.getId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam(name = "range") RemovalRange range,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        recurringTransactionService.delete(id, range, userDetails.getId());

        return ResponseEntity.noContent().build();
    }
}
