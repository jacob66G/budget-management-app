package com.example.budget_management_app.transaction.controller;

import com.example.budget_management_app.security.service.CustomUserDetails;
import com.example.budget_management_app.transaction.domain.SortDirection;
import com.example.budget_management_app.transaction.domain.SortedBy;
import com.example.budget_management_app.transaction.domain.TransactionModeFilter;
import com.example.budget_management_app.transaction.domain.TransactionTypeFilter;
import com.example.budget_management_app.transaction.dto.*;
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
    public ResponseEntity<PagedResponse<TransactionView>> getPage(
            @RequestParam(name = "page") int page,
            @RequestParam(name = "limit") int limit,
            @RequestParam(name = "type", defaultValue = "ALL") TransactionTypeFilter type,
            @RequestParam(name = "mode", defaultValue = "ALL")TransactionModeFilter mode,
            @RequestParam(name = "accounts") List<Long> accounts,
            @RequestParam(name = "categories") List<Long> categories,
            @RequestParam(name = "since") LocalDate since,
            @RequestParam(name = "to", required = false) LocalDate to,
            @RequestParam(name = "sortedBy", defaultValue = "DATE") SortedBy sortedBy,
            @RequestParam(name = "sortDirection", defaultValue = "DESC") SortDirection sortDirection,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        return ResponseEntity.ok(transactionService.getViews(
                page,
                limit,
                type,
                mode,
                accounts,
                categories,
                since,
                to,
                sortedBy,
                sortDirection,
                userDetails.getId()));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @RequestBody TransactionCreateRequest transactionCreate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TransactionResponse response = transactionService.create(transactionCreate, userDetails.getId());

        URI location = URI.create("/api/v1/transactions/" + response.id());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @PatchMapping("/{id}/category")
    public ResponseEntity<TransactionCategoryUpdateResponse> changeCategory(
            @PathVariable long id,
            @RequestBody TransactionCategoryUpdateRequest updateReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ResponseEntity.ok(transactionService.changeCategory(
                id,
                userDetails.getId(),
                updateReq
        ));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable long id,
            @RequestBody TransactionUpdateRequest updateReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        transactionService.update(id, userDetails.getId(), updateReq);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        transactionService.delete(id, userDetails.getId());

        return ResponseEntity.noContent().build();
    }
}
