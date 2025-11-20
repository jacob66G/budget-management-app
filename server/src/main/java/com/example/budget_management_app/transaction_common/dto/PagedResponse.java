package com.example.budget_management_app.transaction_common.dto;

import java.util.List;
import java.util.Map;

public record PagedResponse<T>(
        List<T> data,
        Pagination pagination,
        Map<String, String> links
) {
    public static <T> PagedResponse<T> of(List<T> data, int page, int limit, long totalElements){

        Pagination pagination = Pagination.of(page, limit, totalElements, data.size());
        return new PagedResponse<>(data, pagination, null);
    }

    public PagedResponse<T> withLinks(Map<String, String> newLinks) {
        return new PagedResponse<>(data, pagination, newLinks);
    }
}
