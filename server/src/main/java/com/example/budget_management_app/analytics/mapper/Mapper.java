package com.example.budget_management_app.analytics.mapper;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.analytics.dto.AccountSummaryDto;
import com.example.budget_management_app.analytics.dto.TransactionSummaryDto;
import com.example.budget_management_app.common.enums.SupportedCurrency;
import com.example.budget_management_app.common.storage.service.StorageService;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class Mapper {

    private final StorageService storageService;

    public TransactionSummaryDto toTransactionSummary(Tuple tuple) {
        return new TransactionSummaryDto(
                tuple.get("title", String.class),
                tuple.get("amount", BigDecimal.class),
                tuple.get("type", TransactionType.class),
                tuple.get("transactionDate", LocalDateTime.class),
                tuple.get("categoryName", String.class),
                storageService.getPublicUrl(tuple.get("iconKey", String.class)),
                tuple.get("accountName", String.class),
                tuple.get("currency", SupportedCurrency.class)
        );
    }

    public AccountSummaryDto toAccountSummary(Account account) {
        return new AccountSummaryDto(
                account.getId(),
                account.getName(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.isIncludeInTotalBalance(),
                storageService.getPublicUrl(account.getIconKey())
        );
    }
}
