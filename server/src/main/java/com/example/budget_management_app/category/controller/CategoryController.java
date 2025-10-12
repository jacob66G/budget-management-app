package com.example.budget_management_app.category.controller;

import com.example.budget_management_app.category.dto.CategoryCreateRequestDto;
import com.example.budget_management_app.category.dto.CategoryResponseDto;
import com.example.budget_management_app.category.dto.CategoryUpdateRequestDto;
import com.example.budget_management_app.category.service.CategoryService;
import com.example.budget_management_app.constants.ApiPaths;
import com.example.budget_management_app.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getCategories(
            @RequestParam(required = false) String type,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        return ResponseEntity.ok(categoryService.getCategories(userId, type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        return ResponseEntity.ok(categoryService.getCategory(userId, id));
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> addCategory(
            @RequestBody CategoryCreateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CategoryResponseDto response = categoryService.addCategory(userDetails.getId(), requestDto);
        return ResponseEntity.created(UriComponentsBuilder.fromPath(ApiPaths.BASE_API)
                        .pathSegment(ApiPaths.CATEGORIES)
                        .pathSegment(String.valueOf(response.id()))
                        .build().toUri()
                )
                .body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryUpdateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CategoryResponseDto response = categoryService.updateCategory(userDetails.getId(), id, requestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        categoryService.deleteCategory(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
