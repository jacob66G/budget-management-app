package com.example.budget_management_app.transaction.mapper;

import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction.domain.TransactionType;
import com.example.budget_management_app.transaction.dto.AccountSummary;
import com.example.budget_management_app.transaction.dto.CategorySummary;
import com.example.budget_management_app.transaction.dto.TransactionCreate;
import com.example.budget_management_app.transaction.dto.TransactionView;
import jakarta.persistence.Tuple;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Mapper {

    private Mapper(){}

    public static List<TransactionView> toDto(List<Tuple> tuples) {

        return tuples.stream()
                .map( tuple -> new TransactionView(
                        tuple.get("transactionId", Long.class),
                        tuple.get("amount", BigDecimal.class),
                        tuple.get("type", TransactionType.class),
                        tuple.get("description", String.class),
                        tuple.get("transactionDate", LocalDateTime.class),
                        new AccountSummary(
                                tuple.get("accountId", Long.class),
                                tuple.get("accountName", String.class)
                        ),
                        new CategorySummary(
                                tuple.get("categoryId", Long.class),
                                tuple.get("categoryName", String.class),
                                tuple.get("iconPath", String.class)
                        )
                ))
                .toList();
    }

    public static Transaction fromDto(TransactionCreate transactionCreate) {
        return new Transaction(
                transactionCreate.amount(),
                transactionCreate.title(),
                transactionCreate.type(),
                transactionCreate.description(),
                LocalDateTime.now()
        );
    }
}
