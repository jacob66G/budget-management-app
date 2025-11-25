package com.example.budget_management_app.chat.service;

import com.example.budget_management_app.chat.dto.TransactionDataResponse;
import com.example.budget_management_app.chat.dto.TransactionRangeRequest;
import com.example.budget_management_app.security.service.CustomUserDetails;
import com.example.budget_management_app.transaction.dao.TransactionDao;
import com.example.budget_management_app.transaction.mapper.TransactionMapper;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpendingAnalysisService{

    private final TransactionDao transactionDao;
    private final TransactionMapper mapper;

    @Tool(description = "Returns user expense history within a given date range for specific account.")
    public List<TransactionDataResponse> getUserExpenseHistoryForAccount(TransactionRangeRequest request) {

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(request.startDate());
            endDate = LocalDate.parse(request.endDate());
        } catch (Exception e) {
            startDate = LocalDate.now().withDayOfMonth(1);
            endDate = LocalDate.now();
        }

        Long userId = this.getCurrentUserId();
        List<Tuple> results = this.transactionDao.getUserExpensesByAccount(
                startDate,
                endDate,
                request.accountName(),
                userId
        );
        return mapper.toTransactionDataResponse(results);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getId();
        }

        throw new IllegalStateException("The user is not logged in!");
    }
}
