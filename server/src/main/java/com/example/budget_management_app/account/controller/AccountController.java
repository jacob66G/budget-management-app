package com.example.budget_management_app.account.controller;

import com.example.budget_management_app.account.dto.*;
import com.example.budget_management_app.account.service.AccountService;
import com.example.budget_management_app.constants.ApiPaths;
import com.example.budget_management_app.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<AccountDetailsResponse> getAccount(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(accountService.getAccount(userDetails.getId(), id));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAccounts(
            SearchCriteria criteria,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(accountService.getAccounts(userDetails.getId(), criteria));
    }

    @PostMapping
    public ResponseEntity<AccountDetailsResponse> addAccount(
            @Valid @RequestBody AccountCreateRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AccountDetailsResponse response = accountService.createAccount(userDetails.getId(), dto);
        return ResponseEntity.created(UriComponentsBuilder.fromPath(ApiPaths.BASE_API)
                        .pathSegment(ApiPaths.VERSIONING)
                        .pathSegment(ApiPaths.ACCOUNTS)
                        .pathSegment(String.valueOf(response.id()))
                        .build().toUri()
                )
                .body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccountDetailsResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(accountService.updateAccount(userDetails.getId(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        accountService.deleteAccount(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<AccountDetailsResponse> activateAccount(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AccountDetailsResponse response = accountService.activateAccount(userDetails.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<AccountDetailsResponse> deactivateAccount(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AccountDetailsResponse response = accountService.deactivateAccount(userDetails.getId(), id);
        return ResponseEntity.ok(response);
    }
}
