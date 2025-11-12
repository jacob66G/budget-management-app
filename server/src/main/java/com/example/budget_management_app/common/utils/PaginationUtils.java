package com.example.budget_management_app.common.utils;

import com.example.budget_management_app.transaction_common.dto.Pagination;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public class PaginationUtils {

    public static Map<String, String> createLinks(Pagination pagination) {

        int currentPage = pagination.page();
        int limit = pagination.limit();
        int totalPages = pagination.totalPages();

        Map<String, String> links = new LinkedHashMap<>();

        UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();

        links.put("self", buildLink(builder, currentPage, limit));

        links.put("first", buildLink(builder, 1, limit));

        if (currentPage < totalPages) {
            links.put("next", buildLink(builder, currentPage + 1, limit));
        }

        if (totalPages > 1) {
            links.put("last", buildLink(builder, totalPages, limit));
        }

        if (currentPage > 1) {
            links.put("prev", buildLink(builder, currentPage - 1, limit));
        }

        return links;
    }

    private static String buildLink(UriComponentsBuilder builder, int page, int limit) {
        return builder.cloneBuilder()
                .replaceQueryParam("page", page)
                .replaceQueryParam("limit", limit)
                .toUriString();
    }
}
