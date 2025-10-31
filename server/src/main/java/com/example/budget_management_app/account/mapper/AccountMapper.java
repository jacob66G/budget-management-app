package com.example.budget_management_app.account.mapper;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.account.dto.AccountDetailsResponseDto;
import com.example.budget_management_app.account.dto.AccountResponseDto;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

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
                account.getIconPath(),
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
                account.getIconPath(),
                account.isIncludeInTotalBalance(),
                account.getCreatedAt(),
                account.getAccountStatus().name()
        );
    }
}
