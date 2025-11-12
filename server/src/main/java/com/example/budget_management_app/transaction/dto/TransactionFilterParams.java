package com.example.budget_management_app.transaction.dto;

import com.example.budget_management_app.transaction.domain.TransactionModeFilter;
import com.example.budget_management_app.transaction.domain.TransactionTypeFilter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFilterParams{

    private TransactionTypeFilter type = TransactionTypeFilter.ALL;
    private TransactionModeFilter mode = TransactionModeFilter.ALL;

    @NotEmpty(message = "Accounts id list must not be empty")
    private List<@NotNull(message = "Account id must not be null") Long> accountIds;

    @NotEmpty(message = "Categories id list must not be empty")
    private List<@NotNull(message = "Category id must not be null") Long> categoryIds;

    @PastOrPresent(message = "since date must not be in the future")
    private LocalDate since = LocalDate.now().minusMonths(1);

    @PastOrPresent(message = "to date must not be in the future")
    private LocalDate to = LocalDate.now();
}
