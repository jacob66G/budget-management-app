package com.example.budget_management_app.recurring_transaction.controller;

import com.example.budget_management_app.recurring_transaction.domain.RemovalRange;
import com.example.budget_management_app.recurring_transaction.dto.*;
import com.example.budget_management_app.recurring_transaction.service.RecurringTransactionService;
import com.example.budget_management_app.security.service.CustomUserDetails;
import com.example.budget_management_app.transaction.dto.PagedResponse;
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

    @GetMapping("/{id}/details")
    public ResponseEntity<RecurringTransactionDetailsResponse> getDetails(
            @PathVariable long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(recurringTransactionService.getDetails(
                id,
                userDetails.getId()
        ));
    }

    @PostMapping
    public ResponseEntity<RecurringTransactionCreateResponse> create(
            @RequestBody RecurringTransactionCreateRequest createReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        RecurringTransactionCreateResponse response = recurringTransactionService.create(userDetails.getId(), createReq);

        URI location = URI.create("/api/v1/recurring-transactions/" + response.id());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable long id,
            @RequestBody RecurringTransactionStatusUpdateRequest updateReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        recurringTransactionService.changeStatus(id, userDetails.getId(), updateReq.isActive());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable long id,
            @RequestParam(name = "range") RemovalRange range,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        recurringTransactionService.delete(id, userDetails.getId(), range);

        return ResponseEntity.noContent().build();
    }
}
