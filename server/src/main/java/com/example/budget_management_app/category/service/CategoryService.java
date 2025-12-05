package com.example.budget_management_app.category.service;

import com.example.budget_management_app.category.domain.CategoryType;
import com.example.budget_management_app.category.dto.CategoryCreateRequest;
import com.example.budget_management_app.category.dto.CategoryResponse;
import com.example.budget_management_app.category.dto.CategoryUpdateRequest;
import com.example.budget_management_app.user.domain.User;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getCategories(Long userId, CategoryType type);

    CategoryResponse getCategory(Long userId, Long categoryId);

    CategoryResponse createCategory(Long userId, CategoryCreateRequest dto);

    CategoryResponse updateCategory(Long userId, Long categoryId, CategoryUpdateRequest dto);

    void deleteCategory(Long userId, Long categoryId);

    void assignInitialCategories(User user);

    void deleteAllByUser(Long userId);

    void reassignTransactions(Long userId, Long oldCategoryId, Long newCategoryId);
}
