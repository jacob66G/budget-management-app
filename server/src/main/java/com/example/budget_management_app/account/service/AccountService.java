package com.example.budget_management_app.account.service;

import com.example.budget_management_app.account.dto.*;
import com.example.budget_management_app.user.domain.User;

import java.util.List;

public interface AccountService {

    AccountDetailsResponse getAccount(Long userId, Long accountId);

    List<AccountResponse> getAccounts(Long userId, SearchCriteria criteria);

    AccountDetailsResponse createAccount(Long userId, AccountCreateRequest dto);

    AccountDetailsResponse updateAccount(Long userId, Long accountId, AccountUpdateRequest dto);

    void createDefaultAccount(User user);

    AccountDetailsResponse activateAccount(Long userId, Long accountId);

    AccountDetailsResponse deactivateAccount(Long userId, Long accountId);

    void activateAllByUser(Long userId);

    void deactivateAllByUser(Long userId);

    void deleteAccount(Long userId, Long accountId);

    void deleteAllByUser(Long userId);
}
