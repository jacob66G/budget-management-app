package com.example.budget_management_app.transaction.mapper;

import com.example.budget_management_app.common.enums.SupportedCurrency;
import com.example.budget_management_app.common.service.StorageService;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.transaction_common.dto.AccountSummary;
import com.example.budget_management_app.transaction_common.dto.CategorySummary;
import com.example.budget_management_app.transaction.dto.TransactionCreateRequest;
import com.example.budget_management_app.transaction.dto.TransactionSummary;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    private final StorageService storageService;

    public List<TransactionSummary> toDto(List<Tuple> tuples) {

        return tuples.stream()
                .map( tuple -> new TransactionSummary(
                        tuple.get("transactionId", Long.class),
                        tuple.get("title", String.class),
                        tuple.get("amount", BigDecimal.class),
                        tuple.get("type", TransactionType.class),
                        tuple.get("description", String.class),
                        tuple.get("transactionDate", LocalDateTime.class),
                        new AccountSummary(
                                tuple.get("accountId", Long.class),
                                tuple.get("accountName", String.class),
                                tuple.get("currency", SupportedCurrency.class)
                        ),
                        new CategorySummary(
                                tuple.get("categoryId", Long.class),
                                tuple.get("categoryName", String.class),
                                storageService.getPublicUrl(tuple.get("iconKey", String.class))
                        ),
                        tuple.get("recId", Long.class)
                ))
                .toList();
    }

    public Transaction fromDto(TransactionCreateRequest transactionCreate) {
        return new Transaction(
                transactionCreate.amount(),
                transactionCreate.title().trim(),
                transactionCreate.type(),
                transactionCreate.description().trim(),
                LocalDateTime.now()
        );
    }
}
