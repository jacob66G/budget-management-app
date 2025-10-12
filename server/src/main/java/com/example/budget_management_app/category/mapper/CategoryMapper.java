package com.example.budget_management_app.category.mapper;

import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.category.dto.CategoryResponseDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDto toCategoryResponseDto(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getType(),
                category.isDefault(),
                category.getIconPath()
        );
    }
}
