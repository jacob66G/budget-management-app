package com.example.budget_management_app.transaction.dto;

import java.util.List;

public record PagedResponse<T>(
        List<T> data,
        Pagination pagination
) {
    public static <T> PagedResponse<T> of(List<T> data, int page, int size, long totalElements){

        Pagination pagination = Pagination.of(page, size, totalElements, data.size());
        return new PagedResponse<>(data, pagination);
    }
}
