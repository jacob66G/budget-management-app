package com.example.budget_management_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BudgetManagementAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudgetManagementAppApplication.class, args);
	}

}
