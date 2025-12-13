package com.example.budget_management_app;

import com.example.budget_management_app.common.storage.init.S3IconInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class BudgetManagementAppApplicationTests {

    @MockitoBean
    private S3IconInitializer s3IconInitializer;

    @Test
    void contextLoads() {
    }

}
