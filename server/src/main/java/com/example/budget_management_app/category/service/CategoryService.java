package com.example.budget_management_app.category.service;

import com.example.budget_management_app.category.dto.CategoryCreateRequestDto;
import com.example.budget_management_app.category.dto.CategoryResponseDto;
import com.example.budget_management_app.category.dto.CategoryUpdateRequestDto;
import com.example.budget_management_app.user.domain.User;

import java.util.List;

public interface CategoryService {

    List<CategoryResponseDto> getCategories(Long userId, String type);

    CategoryResponseDto getCategory(Long userId, Long categoryId);

    CategoryResponseDto addCategory(Long userId, CategoryCreateRequestDto dto);

    CategoryResponseDto updateCategory(Long userId, Long categoryId, CategoryUpdateRequestDto dto);

    void deleteCategory(Long userId, Long categoryId);

    void assignInitialCategories(User user);
}
