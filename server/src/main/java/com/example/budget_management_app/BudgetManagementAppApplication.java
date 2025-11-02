package com.example.budget_management_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class BudgetManagementAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudgetManagementAppApplication.class, args);
	}

}
