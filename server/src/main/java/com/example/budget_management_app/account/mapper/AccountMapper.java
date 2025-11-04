package com.example.budget_management_app.account.mapper;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.account.dto.AccountDetailsResponseDto;
import com.example.budget_management_app.account.dto.AccountResponseDto;
import com.example.budget_management_app.common.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountMapper {

    private final StorageService storageService;

    public AccountResponseDto toResponseDto(Account account) {
        return new AccountResponseDto(
                account.getId(),
                account.getAccountType().name(),
                account.getName(),
                account.getBalance(),
                account.getTotalIncome(),
                account.getTotalExpense(),
                account.getCurrency().name(),
                account.isDefault(),
                storageService.getPublicUrl(account.getIconKey()),
                account.isIncludeInTotalBalance(),
                account.getCreatedAt(),
                account.getAccountStatus().name()
        );
    }

    public AccountDetailsResponseDto toDetailsResponseDto(Account account) {
        return new AccountDetailsResponseDto(
                account.getId(),
                account.getAccountType().name(),
                account.getName(),
                account.getBalance(),
                account.getTotalIncome(),
                account.getTotalExpense(),
                account.getCurrency().name(),
                account.isDefault(),
                account.getDescription(),
                account.getBudgetType().name(),
                account.getBudget(),
                account.getAlertThreshold(),
                storageService.getPublicUrl(account.getIconKey()),
                account.isIncludeInTotalBalance(),
                account.getCreatedAt(),
                account.getAccountStatus().name()
        );
    }
}
