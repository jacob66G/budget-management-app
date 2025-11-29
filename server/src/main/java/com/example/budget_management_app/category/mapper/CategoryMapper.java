package com.example.budget_management_app.category.mapper;

import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.category.dto.CategoryResponse;
import com.example.budget_management_app.common.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {

    private final StorageService storageService;

    public CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.isDefault(),
                storageService.getPublicUrl(category.getIconKey())
        );
    }
}
