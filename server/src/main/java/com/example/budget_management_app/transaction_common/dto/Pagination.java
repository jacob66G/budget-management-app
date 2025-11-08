package com.example.budget_management_app.transaction_common.dto;

public record Pagination(
        int page,
        int limit,
        int size,
        long totalCount,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static Pagination of(int page, int limit, long totalElements, int numberOfElements) {

        int totalPages = (int) Math.ceil( (double) totalElements / limit);
        boolean hasNext = page < totalPages;
        boolean hasPrevious = page > 1;

        return new Pagination(
                page,
                limit,
                numberOfElements,
                totalElements,
                totalPages,
                hasNext,
                hasPrevious
        );
    }
}
