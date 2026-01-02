package com.example.budget_management_app.category.service;

import com.example.budget_management_app.category.dao.CategoryDao;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.category.domain.CategoryType;
import com.example.budget_management_app.category.domain.InitialCategory;
import com.example.budget_management_app.category.dto.CategoryCreateRequest;
import com.example.budget_management_app.category.dto.CategoryResponse;
import com.example.budget_management_app.category.dto.CategoryUpdateRequest;
import com.example.budget_management_app.category.mapper.CategoryMapper;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.ValidationException;
import com.example.budget_management_app.common.validation.IconKeyValidator;
import com.example.budget_management_app.common.storage.service.StorageService;
import com.example.budget_management_app.recurring_transaction.service.RecurringTransactionService;
import com.example.budget_management_app.transaction.service.TransactionService;
import com.example.budget_management_app.user.dao.UserDao;
import com.example.budget_management_app.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;
    private final UserDao userDao;
    private final StorageService storageService;
    private final CategoryMapper mapper;
    private final IconKeyValidator iconKeyValidator;
    private final TransactionService transactionService;
    private final RecurringTransactionService recurringTransactionService;

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> getCategories(Long userId, CategoryType type) {
        return categoryDao.findByUser(userId, type).stream().map(mapper::toCategoryResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryResponse getCategory(Long userId, Long categoryId) {
        Category category = categoryDao.findByIdAndUser(categoryId, userId)
                .orElseThrow(() -> new NotFoundException(Category.class.getSimpleName(), categoryId, ErrorCode.NOT_FOUND));
        return mapper.toCategoryResponse(category);
    }

    @Transactional
    @Override
    public CategoryResponse createCategory(Long userId, CategoryCreateRequest dto) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id: " + userId + " not found", ErrorCode.NOT_FOUND));
        validateNameUniqueness(userId, dto.name(), null);

        Category category = new Category();
        category.setName(dto.name());
        category.setType(dto.type());
        category.setDefault(false);

        String iconKey = storageService.extractKey(dto.iconPath());
        validateIcon(iconKey);
        category.setIconKey(iconKey);

        user.addCategory(category);

        log.info("User: {} has created new category: {}", userId, dto.name());
        return mapper.toCategoryResponse(categoryDao.save(category));
    }

    @Transactional
    @Override
    public CategoryResponse updateCategory(Long userId, Long categoryId, CategoryUpdateRequest dto) {
        Category category = categoryDao.findByIdAndUser(categoryId, userId)
                .orElseThrow(() -> new NotFoundException(Category.class.getSimpleName(), categoryId, ErrorCode.NOT_FOUND));

        validateCategoryCanBeModify(category);

        if (StringUtils.hasText(dto.name()) && !category.getName().equals(dto.name())) {
            validateNameUniqueness(userId, dto.name(), categoryId);
            category.setName(dto.name());
        }
        if (dto.type() != null && category.getType() != dto.type()) {
            if (isCategoryUsed(categoryId, userId)) {
                throw new ValidationException(
                        "Cannot change the type of a category that already has transactions assigned to it.",
                        ErrorCode.CATEGORY_IS_IN_USE
                );
            }
            category.setType(dto.type());
        }
        if (StringUtils.hasText(dto.iconPath()) && !category.getIconKey().equals(dto.iconPath())) {
            String iconKey = storageService.extractKey(dto.iconPath());
            validateIcon(iconKey);
            category.setIconKey(iconKey);
        }

        log.info("User: {} has updated category: {}", userId, categoryId);
        return mapper.toCategoryResponse(categoryDao.update(category));
    }

    @Transactional
    @Override
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryDao.findByIdAndUser(categoryId, userId)
                .orElseThrow(() -> new NotFoundException(Category.class.getSimpleName(), categoryId, ErrorCode.NOT_FOUND));

        validateCategoryCanBeModify(category);
        if (isCategoryUsed(categoryId, userId)) {
            throw new ValidationException(
                    "Cannot delete a category that already has transactions assigned to it.",
                    ErrorCode.CATEGORY_IS_IN_USE
            );
        }

        categoryDao.delete(category);
        log.info("User: {} has deleted category: {}", userId, categoryId);
    }

    @Override
    public void assignInitialCategories(User user) {
        for (InitialCategory def : InitialCategory.values()) {
            Category category = new Category();
            category.setName(def.getName());
            category.setType(def.getType());
            category.setDefault(def.isDefault());
            category.setIconKey(def.getIconKey());

            user.addCategory(category);
        }
    }

    @Transactional
    @Override
    public void deleteAllByUser(Long userId) {
        categoryDao.deleteAllByUser(userId);
        log.info("Deleting all user: {} categories", userId);
    }

    @Transactional
    @Override
    public void reassignTransactions(Long userId, Long oldCategoryId, Long newCategoryId) {
        if (oldCategoryId.equals(newCategoryId)) {
            throw new ValidationException("Cannot reassign to the same category.", ErrorCode.WRONG_CATEGORY_TYPE);
        }

        Category oldCategory = categoryDao.findByIdAndUser(oldCategoryId, userId)
                .orElseThrow(() -> new NotFoundException("Old category not found", ErrorCode.NOT_FOUND));

        Category newCategory = categoryDao.findByIdAndUser(newCategoryId, userId)
                .orElseThrow(() -> new NotFoundException("New category not found", ErrorCode.NOT_FOUND));

        validateCategoryReassignment(oldCategory.getType(), newCategory.getType());

        transactionService.reassignCategoryForUser(userId, oldCategoryId, newCategoryId);
        recurringTransactionService.reassignCategoryForUser(userId, oldCategoryId, newCategoryId);
    }

    private boolean isCategoryUsed(Long categoryId, Long userId) {
        return transactionService.existsByCategoryAndUser(categoryId, userId) || recurringTransactionService.existsByCategoryAndUser(categoryId, userId);
    }

    private void validateCategoryReassignment(CategoryType oldType, CategoryType newType) {
        if (oldType == newType) {
            return;
        }

        if (newType.equals(CategoryType.GENERAL)) {
            return;
        }

        if (oldType.equals(CategoryType.GENERAL) && (newType.equals(CategoryType.EXPENSE) || newType.equals(CategoryType.INCOME))) {
            throw new ValidationException(
                    "Cannot reassign from a 'GENERAL' category (which contains mixed transactions) to a specific type (INCOME/EXPENSE).",
                    ErrorCode.WRONG_CATEGORY_TYPE
            );
        }

        if (oldType.equals(CategoryType.EXPENSE) && newType.equals(CategoryType.INCOME) ||
                oldType.equals(CategoryType.INCOME) && newType.equals(CategoryType.EXPENSE)) {
            throw new ValidationException(
                    "Cannot reassign transactions between INCOME and EXPENSE categories.",
                    ErrorCode.WRONG_CATEGORY_TYPE
            );
        }
    }

    private void validateNameUniqueness(Long userId, String name, Long existingCategoryId) {
        if (categoryDao.existsByNameAndUser(name, userId, existingCategoryId)) {
            throw new ValidationException("You already have category with name: " + name, ErrorCode.NAME_ALREADY_USED);
        }
    }

    private void validateIcon(String key) {
        if (key == null) {
            return;
        }

        if (!iconKeyValidator.isValidCategoryIconKey(key)) {
            log.warn("Invalid icon key: {}", key);
            throw new ValidationException("Selected icon is not valid.", ErrorCode.INVALID_RESOURCE_PATH);
        }
    }

    private void validateCategoryCanBeModify(Category category) {
        if (category.isDefault()) {
            throw new ValidationException("This category is default and cannot be modified", ErrorCode.MODIFY_DEFAULT_CATEGORY);
        }
    }
}
