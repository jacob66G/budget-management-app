package com.example.budget_management_app.transaction.controller;

import com.example.budget_management_app.common.utils.PaginationUtils;
import com.example.budget_management_app.security.service.CustomUserDetails;
import com.example.budget_management_app.transaction.dto.*;
import com.example.budget_management_app.transaction.service.TransactionService;
import com.example.budget_management_app.transaction_common.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/transactions", produces = {MediaType.APPLICATION_JSON_VALUE})
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<PagedResponse<TransactionSummary>> getSummariesPage(
            TransactionPageRequest pageReq,
            TransactionSearchCriteria searchCriteria,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        PagedResponse<TransactionSummary> summariesPage = transactionService.getSummariesPage(
                pageReq,
                searchCriteria,
                userDetails.getId());

        return ResponseEntity.ok(summariesPage.withLinks(PaginationUtils.createLinks(summariesPage.pagination())));
    }

    @PostMapping
    public ResponseEntity<TransactionCreateResponse> create(
            @RequestBody TransactionCreateRequest transactionCreate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TransactionCreateResponse response = transactionService.create(transactionCreate, userDetails.getId());

        URI location = URI.create("/api/v1/transactions/" + response.id());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @PatchMapping("/{id}/category")
    public ResponseEntity<TransactionCategoryUpdateResponse> changeCategory(
            @PathVariable Long id,
            @RequestBody TransactionCategoryUpdateRequest updateReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ResponseEntity.ok(transactionService.changeCategory(
                id,
                updateReq,
                userDetails.getId()
        ));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody TransactionUpdateRequest updateReq,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        transactionService.update(id, updateReq, userDetails.getId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        transactionService.delete(id, userDetails.getId());

        return ResponseEntity.noContent().build();
    }
}
