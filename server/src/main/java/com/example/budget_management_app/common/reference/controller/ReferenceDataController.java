package com.example.budget_management_app.common.reference.controller;


import com.example.budget_management_app.account.domain.AccountStatus;
import com.example.budget_management_app.account.domain.AccountType;
import com.example.budget_management_app.account.domain.BudgetType;
import com.example.budget_management_app.category.domain.CategoryType;
import com.example.budget_management_app.common.enums.SupportedCurrency;
import com.example.budget_management_app.common.service.StorageService;
import com.example.budget_management_app.config.IconConfig;
import com.example.budget_management_app.session.domain.DeviceType;
import com.example.budget_management_app.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/reference-data")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final IconConfig iconConfig;
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<ReferenceDataResponse> getReferenceData() {
        return ResponseEntity.ok(new ReferenceDataResponse(
                Arrays.stream(AccountType.values()).map(Enum::name).toList(),
                Arrays.stream(BudgetType.values()).map(Enum::name).toList(),
                Arrays.stream(SupportedCurrency.values()).map(Enum::name).toList(),
                Arrays.stream(CategoryType.values()).map(Enum::name).toList(),
                Arrays.stream(DeviceType.values()).map(Enum::name).toList(),
                Arrays.stream(UserStatus.values()).map(Enum::name).toList(),
                Arrays.stream(AccountStatus.values()).map(Enum::name).toList(),
                iconConfig.getAccounts().stream()
                        .map(storageService::getPublicUrl)
                        .toList(),

                iconConfig.getCategories().stream()
                        .map(storageService::getPublicUrl)
                        .toList()
        ));
    }

    public record ReferenceDataResponse(
            List<String> accountTypes,
            List<String> budgetTypes,
            List<String> currencies,
            List<String> categoryTypes,
            List<String> deviceTypes,
            List<String> userStatuses,
            List<String> accountStatuses,
            List<String> accountIcons,
            List<String> categoryIcons
    ) {
    }

}
