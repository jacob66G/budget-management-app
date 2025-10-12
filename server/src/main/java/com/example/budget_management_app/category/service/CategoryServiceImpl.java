package com.example.budget_management_app.category.service;

import com.example.budget_management_app.category.dao.CategoryDao;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.category.domain.CategoryType;
import com.example.budget_management_app.category.domain.InitialCategory;
import com.example.budget_management_app.category.dto.CategoryCreateRequestDto;
import com.example.budget_management_app.category.dto.CategoryResponseDto;
import com.example.budget_management_app.category.dto.CategoryUpdateRequestDto;
import com.example.budget_management_app.category.mapper.CategoryMapper;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.common.exception.ValidationException;
import com.example.budget_management_app.common.service.StorageService;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.service.UserService;
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
    private final UserService userService;
    private final StorageService storageService;
    private final CategoryMapper mapper;

    @Override
    public List<CategoryResponseDto> getCategories(Long userId, String type) {
        return categoryDao.findByUser(userId, type).stream().map(mapper::toCategoryResponseDto).toList();
    }

    @Override
    public CategoryResponseDto getCategory(Long userId, Long categoryId) {
        Category category = categoryDao.findByIdAndUser(categoryId, userId)
                .orElseThrow(() -> new NotFoundException(Category.class.getSimpleName(), categoryId, ErrorCode.NOT_FOUND));
        return mapper.toCategoryResponseDto(category);
    }

    @Transactional
    @Override
    public CategoryResponseDto addCategory(Long userId, CategoryCreateRequestDto dto) {
        User user = userService.getUserById(userId);
        validateCategory(dto, user.getId());

        Category category = new Category();
        category.setName(dto.name());
        category.setType(CategoryType.valueOf(dto.type().toUpperCase()));
        category.setDefault(false);
        category.setIconPath(dto.iconPath());
        category.setUser(user);

        return mapper.toCategoryResponseDto(categoryDao.save(category));
    }

    @Transactional
    @Override
    public CategoryResponseDto updateCategory(Long userId, Long categoryId, CategoryUpdateRequestDto dto) {
        Category category = categoryDao.findByIdAndUser(categoryId, userId)
                .orElseThrow(() -> new NotFoundException(Category.class.getSimpleName(), categoryId, ErrorCode.NOT_FOUND));

        if (StringUtils.hasText(dto.name())) {
            validateNameUniqueness(userId, dto.name(), categoryId);
            category.setName(dto.name());
        }
        if (StringUtils.hasText(dto.type())) {
            validateCategoryCanBeModify(category);
            validateType(dto.type());
            category.setType(CategoryType.valueOf(dto.type().toUpperCase()));
        }
        if (StringUtils.hasText(dto.iconPath())) {
            validateIconPathExists(dto.iconPath());
            category.setIconPath(dto.iconPath());
        }

        return mapper.toCategoryResponseDto(categoryDao.update(category));
    }

    @Transactional
    @Override
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryDao.findByIdAndUser(categoryId, userId)
                .orElseThrow(() -> new NotFoundException(Category.class.getSimpleName(), categoryId, ErrorCode.NOT_FOUND));

        validateCategoryCanBeModify(category);
        categoryDao.delete(category);
    }

    @Override
    public void assignInitialCategories(User user) {
        for (InitialCategory def: InitialCategory.values()) {
            Category category = new Category();
            category.setName(def.getName());
            category.setType(def.getType());
            category.setDefault(def.isDefault());
            category.setIconPath(def.getIconPath());

            user.addCategory(category);
        }
    }

    private void validateCategory(CategoryCreateRequestDto categoryRequest, Long userId) {
        validateNameUniqueness(userId, categoryRequest.name(), null);
        validateType(categoryRequest.type());
        validateIconPathExists(categoryRequest.iconPath());
    }

    private void validateNameUniqueness(Long userId, String name, Long categoryId) {
        if (categoryDao.existsByNameAndUser(name, userId, categoryId)) {
            throw new ValidationException("You already have category with name: " + name, ErrorCode.NAME_ALREADY_USED);
        }
    }

    private void validateType(String type) {
        try {
            CategoryType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Incorrect category type provided: {}", type);
            throw new ValidationException("Incorrect category type: " + type, ErrorCode.WRONG_CATEGORY_TYPE);
        }
    }

    private void validateIconPathExists(String path) {
        if (!storageService.exists(path)) {
            log.warn("Resource with path: {} does not exists in storage", path);
            throw new NotFoundException("This image does not exist", ErrorCode.NOT_FOUND);
        }
    }

    private void validateCategoryCanBeModify(Category category) {
        if (category.isDefault()) {
            throw new ValidationException("This category is default and cannot be modify", ErrorCode.MODIFY_DEFAULT_CATEGORY);
        }

        //TODO check is category is assign to transaction/recurring
    }
}
