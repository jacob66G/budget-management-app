package com.example.budget_management_app.account.service;

import com.example.budget_management_app.account.dto.*;
import com.example.budget_management_app.user.domain.User;

import java.util.List;

public interface AccountService {

    AccountDetailsResponseDto getAccount(Long userId, Long accountId);

    List<AccountResponseDto> getAccounts(Long userId, SearchCriteria criteria);

    AccountDetailsResponseDto createAccount(Long userId, AccountCreateRequestDto dto);

    AccountDetailsResponseDto updateAccount(Long userId, Long accountId, AccountUpdateRequestDto dto);

    void createDefaultAccount(User user);

    void activateAccount(Long userId, Long accountId);

    void deactivateAccount(Long userId, Long accountId);

    void activateAllUserAccounts(Long id);

    void deactivateAllUserAccounts(Long userId);

    void deleteAccount(Long userId, Long accountId);

    void deleteAllUserAccounts(Long userId);
}
