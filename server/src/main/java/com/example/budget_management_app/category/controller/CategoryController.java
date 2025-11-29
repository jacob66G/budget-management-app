package com.example.budget_management_app.category.controller;

import com.example.budget_management_app.category.dto.CategoryCreateRequest;
import com.example.budget_management_app.category.dto.CategoryResponse;
import com.example.budget_management_app.category.dto.CategoryUpdateRequest;
import com.example.budget_management_app.category.dto.ReassignTransactionsRequest;
import com.example.budget_management_app.category.service.CategoryService;
import com.example.budget_management_app.constants.ApiPaths;
import com.example.budget_management_app.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @RequestParam(required = false) String type,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        return ResponseEntity.ok(categoryService.getCategories(userId, type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        return ResponseEntity.ok(categoryService.getCategory(userId, id));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> addCategory(
            @Valid @RequestBody CategoryCreateRequest requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CategoryResponse response = categoryService.createCategory(userDetails.getId(), requestDto);
        return ResponseEntity.created(UriComponentsBuilder.fromPath(ApiPaths.BASE_API)
                        .pathSegment(ApiPaths.CATEGORIES)
                        .pathSegment(String.valueOf(response.id()))
                        .build().toUri()
                )
                .body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CategoryResponse response = categoryService.updateCategory(userDetails.getId(), id, requestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{oldCategoryId}/reassign")
    public ResponseEntity<Void> reassignTransactions(
            @PathVariable Long oldCategoryId,
            @Valid @RequestBody ReassignTransactionsRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        categoryService.reassignTransactions(
                userDetails.getId(),
                oldCategoryId,
                dto.newCategoryId()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        categoryService.deleteCategory(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
