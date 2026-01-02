package com.example.budget_management_app.user.scheduler;

import com.example.budget_management_app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupJob {

    private final UserService userService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void performUserDeletion() {
        log.info("Starting the UserCleanupJob task...");
        userService.deleteUsersPendingDeletion();
        log.info("The task of cleaning users has been completed.");
    }
}
