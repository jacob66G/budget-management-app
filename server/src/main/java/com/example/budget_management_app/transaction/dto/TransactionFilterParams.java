package com.example.budget_management_app.transaction.dto;

import com.example.budget_management_app.transaction.domain.TransactionModeFilter;
import com.example.budget_management_app.transaction.domain.TransactionTypeFilter;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFilterParams{

    private TransactionTypeFilter type = TransactionTypeFilter.ALL;
    private TransactionModeFilter mode = TransactionModeFilter.ALL;

    private List<Long> accountIds = new ArrayList<>();

    private List<Long> categoryIds = new ArrayList<>();

    @PastOrPresent(message = "since date must not be in the future")
    private LocalDate since = LocalDate.now().minusMonths(1);

    @PastOrPresent(message = "to date must not be in the future")
    private LocalDate to = LocalDate.now();
}
