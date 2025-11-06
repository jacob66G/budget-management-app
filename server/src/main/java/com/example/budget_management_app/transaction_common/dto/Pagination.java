package com.example.budget_management_app.transaction_common.dto;

import java.util.Map;

public record Pagination(
        int number,
        int size,
        int numberOfElements,
        long totalCount,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious,
        Map<String, String> links
) {
    public static Pagination of(int page, int size, long totalElements, int numberOfElements) {

        int totalPages = (int) Math.ceil( (double) totalElements / size);
        boolean hasNext = page < totalPages;
        boolean hasPrevious = page > 1;

        return new Pagination(
                page,
                size,
                numberOfElements,
                totalElements,
                totalPages,
                hasNext,
                hasPrevious,
                null
        );
    }
}
