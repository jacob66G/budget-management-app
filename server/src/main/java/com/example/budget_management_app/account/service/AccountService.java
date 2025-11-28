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

    AccountDetailsResponseDto activateAccount(Long userId, Long accountId);

    AccountDetailsResponseDto deactivateAccount(Long userId, Long accountId);

    void activateAllByUser(Long userId);

    void deactivateAllByUser(Long userId);

    void deleteAccount(Long userId, Long accountId);

    void deleteAllByUser(Long userId);
}
