package com.example.budget_management_app.account.mapper;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.account.dto.AccountDetailsResponse;
import com.example.budget_management_app.account.dto.AccountResponse;
import com.example.budget_management_app.common.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountMapper {

    private final StorageService storageService;

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountType(),
                account.getName(),
                account.getBalance(),
                account.getTotalIncome(),
                account.getTotalExpense(),
                account.getCurrency(),
                account.isDefault(),
                storageService.getPublicUrl(account.getIconKey()),
                account.isIncludeInTotalBalance(),
                account.getCreatedAt(),
                account.getAccountStatus()
        );
    }

    public AccountDetailsResponse toDetailsResponse(Account account, boolean hasTransactions) {
        return new AccountDetailsResponse(
                account.getId(),
                account.getAccountType(),
                account.getName(),
                account.getBalance(),
                account.getTotalIncome(),
                account.getTotalExpense(),
                account.getCurrency(),
                account.isDefault(),
                account.getDescription(),
                account.getBudgetType(),
                account.getBudget(),
                account.getAlertThreshold(),
                storageService.getPublicUrl(account.getIconKey()),
                account.isIncludeInTotalBalance(),
                account.getCreatedAt(),
                account.getAccountStatus(),
                hasTransactions
        );
    }
}
