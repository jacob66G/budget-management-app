package com.example.budget_management_app.common.storage.config;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
@Getter
public class IconConfig {

    private final Set<String> categories = new HashSet<>();
    private final Set<String> accounts = new HashSet<>();

    public void addCategoryKey(String key) {
        this.categories.add(key);
    }

    public void addAccountKey(String key) {
        this.accounts.add(key);
    }

}
