package com.example.budget_management_app.category.mapper;

import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.category.dto.CategoryResponseDto;
import com.example.budget_management_app.common.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {

    private final StorageService storageService;

    public CategoryResponseDto toCategoryResponseDto(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getType(),
                category.isDefault(),
                storageService.getPublicUrl(category.getIconKey())
        );
    }
}
