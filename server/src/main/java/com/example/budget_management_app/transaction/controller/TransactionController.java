package com.example.budget_management_app.transaction.controller;

import com.example.budget_management_app.security.service.CustomUserDetails;
import com.example.budget_management_app.transaction.domain.SortDirection;
import com.example.budget_management_app.transaction.domain.SortedBy;
import com.example.budget_management_app.transaction.domain.TransactionModeFilter;
import com.example.budget_management_app.transaction.domain.TransactionTypeFilter;
import com.example.budget_management_app.transaction.dto.PagedResponse;
import com.example.budget_management_app.transaction.dto.TransactionCreate;
import com.example.budget_management_app.transaction.dto.TransactionResponse;
import com.example.budget_management_app.transaction.dto.TransactionView;
import com.example.budget_management_app.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/transactions", produces = {MediaType.APPLICATION_JSON_VALUE})
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<PagedResponse<TransactionView>> getTransactionsPage(
            @RequestParam(name = "page") int page,
            @RequestParam(name = "limit") int limit,
            @RequestParam(name = "type", defaultValue = "ALL") TransactionTypeFilter type,
            @RequestParam(name = "mode", defaultValue = "ALL")TransactionModeFilter mode,
            @RequestParam(name = "accounts") List<Long> accounts,
            @RequestParam(name = "since") LocalDate since,
            @RequestParam(name = "to", required = false) LocalDate to,
            @RequestParam(name = "sortedBy", defaultValue = "DATE") SortedBy sortedBy,
            @RequestParam(name = "sortDirection", defaultValue = "DESC") SortDirection sortDirection
            ) {

        return ResponseEntity.ok(transactionService.getTransactionViews(
                page,
                limit,
                type,
                mode,
                accounts,
                since,
                to,
                sortedBy,
                sortDirection));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody TransactionCreate transactionCreate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TransactionResponse response = transactionService.createTransaction(transactionCreate, userDetails.getId());

        URI location = URI.create("/api/v1/transactions/" + response.id());

        return ResponseEntity
                .created(location)
                .body(response);
    }
}
