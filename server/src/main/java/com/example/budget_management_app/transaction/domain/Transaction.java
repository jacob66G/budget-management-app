package com.example.budget_management_app.transaction.domain;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.recurring_transaction.domain.RecurringTransaction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter@Setter
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @Column(name = "photo_path")
    private String photoPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_transaction_id", insertable = false, updatable = false)
    private RecurringTransaction recurringTransaction;
}
