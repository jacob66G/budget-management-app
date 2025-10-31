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

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<AccountDetailsResponseDto> getAccount(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(accountService.getAccount(userDetails.getId(), id));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> getAccounts(
            @RequestBody SearchCriteria criteria,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(accountService.getAccounts(userDetails.getId(), criteria));
    }

    @PostMapping
    public ResponseEntity<AccountDetailsResponseDto> addAccount(
            @RequestBody AccountCreateRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AccountDetailsResponseDto response = accountService.createAccount(userDetails.getId(), dto);
        return ResponseEntity.created(UriComponentsBuilder.fromPath(ApiPaths.BASE_API)
                        .pathSegment(ApiPaths.ACCOUNTS)
                        .pathSegment(String.valueOf(response.id()))
                        .build().toUri()
                )
                .body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccountDetailsResponseDto> updateAccount(
            @PathVariable Long id,
            @RequestBody AccountUpdateRequestDto dto,
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
    public ResponseEntity<Void> activateAccount(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        accountService.activateAccount(userDetails.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateAccount(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        accountService.deactivateAccount(userDetails.getId(), id);
        return ResponseEntity.ok().build();
    }
}
