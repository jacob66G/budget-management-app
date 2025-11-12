package com.example.budget_management_app.recurring_transaction.controller;

import com.example.budget_management_app.common.utils.PaginationUtils;
import com.example.budget_management_app.constants.ApiPaths;
import com.example.budget_management_app.recurring_transaction.domain.RemovalRange;
import com.example.budget_management_app.recurring_transaction.domain.UpdateRange;
import com.example.budget_management_app.recurring_transaction.dto.*;
import com.example.budget_management_app.recurring_transaction.service.RecurringTransactionService;
import com.example.budget_management_app.security.service.CustomUserDetails;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;
import com.example.budget_management_app.transaction_common.dto.PaginationParams;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(path = "/api/v1/recurring-transactions", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
@Validated
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @GetMapping
    public ResponseEntity<PagedResponse<RecurringTransactionSummary>> getSummariesPage(
            @Valid PaginationParams paginationParams,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        PagedResponse<RecurringTransactionSummary> summariesPage = recurringTransactionService.getSummariesPage(
                paginationParams,
                userDetails.getId());

        return ResponseEntity.ok(summariesPage.withLinks(PaginationUtils.createLinks(summariesPage.pagination())));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<RecurringTransactionDetailsResponse> getDetails(
            @PathVariable @Positive(message = "The recurring transaction id value must be positive") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(recurringTransactionService.getDetails(
                id,
                userDetails.getId()
        ));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<PagedResponse<UpcomingTransactionSummary>> getUpcomingSummaries(
            @Valid PaginationParams paginationParams,
            @Valid UpcomingTransactionFilterParams filterParams,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        PagedResponse<UpcomingTransactionSummary> upcomingTransactionsPage = this.recurringTransactionService.getUpcomingTransactionsPage(
                paginationParams,
                filterParams,
                userDetails.getId()
        );

        return ResponseEntity.ok(
                upcomingTransactionsPage.withLinks(PaginationUtils.createLinks(upcomingTransactionsPage.pagination()))
        );
    }

    @PostMapping
    public ResponseEntity<RecurringTransactionCreateResponse> create(
            @RequestBody @Valid RecurringTransactionCreateRequest createReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        RecurringTransactionCreateResponse response = recurringTransactionService.create(createReq, userDetails.getId());

        return ResponseEntity
                .created(UriComponentsBuilder.fromPath(ApiPaths.BASE_API)
                        .pathSegment(ApiPaths.VERSIONING)
                        .pathSegment(ApiPaths.RECURRING_TRANSACTIONS)
                        .pathSegment(String.valueOf(response.id()))
                        .build().toUri()
                )
                .body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable @Positive(message = "The recurring transaction id value must be positive") Long id,
            @RequestBody @Valid RecurringTransactionStatusUpdateRequest updateReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        recurringTransactionService.changeStatus(id, updateReq.isActive(), userDetails.getId());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable @Positive(message = "The recurring transaction id value must be positive") Long id,
            @RequestParam(name = "range") UpdateRange range,
            @RequestBody @Valid RecurringTransactionUpdateRequest updateReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        recurringTransactionService.update(id, updateReq, range, userDetails.getId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "The recurring transaction id value must be positive") Long id,
            @RequestParam(name = "range") RemovalRange range,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        recurringTransactionService.delete(id, range, userDetails.getId());

        return ResponseEntity.noContent().build();
    }
}
