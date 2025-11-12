package com.example.budget_management_app.recurring_transaction.dto;

import com.example.budget_management_app.recurring_transaction.domain.UpcomingTransactionsTimeRange;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpcomingTransactionFilterParams {

    private UpcomingTransactionsTimeRange range = UpcomingTransactionsTimeRange.NEXT_7_DAYS;

    @NotEmpty(message = "Accounts id list must not be empty")
    private List<@NotNull(message = "Account id must not be null") Long> accountIds;

}
