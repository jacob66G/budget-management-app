package com.example.budget_management_app.chat.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record TransactionRangeRequest(
        @JsonPropertyDescription("Range start date in YYYY-MM-DD format")
        String startDate,

        @JsonPropertyDescription("Range end date in YYYY-MM-DD format")
        String endDate,

        @JsonPropertyDescription("Account name for which the expense history will be retrieved")
        String accountName
) {
}
