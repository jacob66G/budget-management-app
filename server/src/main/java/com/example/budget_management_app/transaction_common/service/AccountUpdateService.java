package com.example.budget_management_app.transaction_common.service;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountUpdateService {

    public void calculateBalanceAfterTransactionDeletion(Account account, BigDecimal amount, TransactionType type) {

        BigDecimal currentBalance = account.getBalance();
        if (type.equals(TransactionType.INCOME)) {
            account.setBalance(currentBalance.subtract(amount));
            account.setTotalIncome(account.getTotalIncome().subtract(amount));
        } else {
            account.setBalance(currentBalance.add(amount));
            account.setTotalExpense(account.getTotalExpense().subtract(amount));
        }
    }

    public void calculateBalanceAfterTransactionCreation(Account account, BigDecimal amount, TransactionType type) {
        BigDecimal currentBalance = account.getBalance();
        if (type.equals(TransactionType.INCOME)) {
            account.setBalance(currentBalance.add(amount));
            account.setTotalIncome(account.getTotalIncome().add(amount));
        } else {
            account.setBalance(currentBalance.subtract(amount));
            account.setTotalExpense(account.getTotalExpense().add(amount));
        }
    }

    public void calculateBalanceAfterTransactionUpdate(Account account, BigDecimal newAmount, BigDecimal currentAmount, TransactionType type) {
        BigDecimal currentBalance = account.getBalance();
        BigDecimal absoluteDifference = newAmount.subtract(currentAmount).abs();
        if (newAmount.compareTo(currentAmount) < 0) {  // new amount is lower
            if (type.equals(TransactionType.INCOME)) {
                account.setBalance(currentBalance.subtract(absoluteDifference));
                account.setTotalIncome(account.getTotalIncome().subtract(absoluteDifference));
            } else {
                account.setBalance(currentBalance.add(absoluteDifference));
                account.setTotalExpense(account.getTotalExpense().subtract(absoluteDifference));
            }
        } else {    // new amount is greater
            if (type.equals(TransactionType.INCOME)) {
                account.setBalance(currentBalance.add(absoluteDifference));
                account.setTotalIncome(account.getTotalIncome().add(absoluteDifference));
            } else {
                account.setBalance(currentBalance.subtract(absoluteDifference));
                account.setTotalExpense(account.getTotalExpense().add(absoluteDifference));
            }
        }
    }
}
