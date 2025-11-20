package com.example.budget_management_app.transaction_common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginationParams {

    @Min(value = 1, message = "Indexing of pages begins with 1")
    private int page = 1;

    @Min(value = 1, message = "Page limit value must be a positive value")
    @Max(value = 20, message = "Page limit value must not exceed 20")
    private int limit = 5;
}
