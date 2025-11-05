package com.example.budget_management_app.recurring_transaction.mapper;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.account.domain.SupportedCurrency;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.recurring_transaction.domain.RecurringInterval;
import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import com.example.budget_management_app.recurring_transaction.dto.RecurringTransactionDetailsResponse;
import com.example.budget_management_app.recurring_transaction.dto.RecurringTransactionSummary;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction.domain.TransactionType;
import com.example.budget_management_app.transaction.dto.AccountSummary;
import com.example.budget_management_app.transaction.dto.CategorySummary;
import com.example.budget_management_app.transaction.dto.TransactionSummary;
import jakarta.persistence.Tuple;

import java.math.BigDecimal;
import java.util.List;

public class Mapper {

    private Mapper(){}

    public static List<RecurringTransactionSummary> fromTuples(List<Tuple> tuples) {

        return tuples.stream()
                .map( tuple -> new RecurringTransactionSummary(
                        tuple.get("recId", Long.class),
                        tuple.get("title", String.class),
                        tuple.get("amount", BigDecimal.class),
                        tuple.get("type", TransactionType.class),
                        tuple.get("isActive", Boolean.class),
                        tuple.get("desc", String.class),
                        tuple.get("recInterval", RecurringInterval.class),
                        tuple.get("recValue", Integer.class),
                        new AccountSummary(
                                tuple.get("accountId", Long.class),
                                tuple.get("accountName", String.class),
                                tuple.get("currency", SupportedCurrency.class)
                        ),
                        new CategorySummary(
                                tuple.get("categoryId", Long.class),
                                tuple.get("categoryName", String.class),
                                tuple.get("iconPath", String.class)
                        )
                )).toList();
    }

    public static RecurringTransactionDetailsResponse toDetails(RecurringTransaction recurringTransaction) {
        return new RecurringTransactionDetailsResponse(
                recurringTransaction.getNextOccurrence(),
                recurringTransaction.getStartDate(),
                recurringTransaction.getEndDate(),
                recurringTransaction.getCreatedAt());
    }

    public static TransactionSummary toTransactionView(Transaction transaction) {
        Account account = transaction.getAccount();
        Category category = transaction.getCategory();
        return new TransactionSummary(transaction.getId(), transaction.getAmount(), transaction.getType(), transaction.getDescription(), transaction.getTransactionDate(),
                new AccountSummary(account.getId(), account.getName(), account.getCurrency()),
                new CategorySummary(category.getId(), category.getName(), category.getIconPath()));
    }
}
