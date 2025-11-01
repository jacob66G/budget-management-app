package com.example.budget_management_app.recurring_transaction.domain;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.transaction.domain.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter@Setter
@NoArgsConstructor
@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
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

    @Column(name = "start_date", nullable = false, updatable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "recurring_interval", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecurringInterval recurringInterval;

    @Column(name = "recurring_value", nullable = false)
    private int recurringValue;

    @Column(name = "next_occurrence", nullable = false)
    private LocalDate nextOccurrence;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "recurringTransaction", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<Transaction> transactions;

    public RecurringTransaction(BigDecimal amount, String title, TransactionType type, String description, LocalDate startDate, LocalDate endDate, RecurringInterval recurringInterval, int recurringValue, LocalDate nextOccurrence, boolean isActive, LocalDateTime createdAt) {
        this.amount = amount;
        this.title = title;
        this.type = type;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recurringInterval = recurringInterval;
        this.recurringValue = recurringValue;
        this.nextOccurrence = nextOccurrence;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public void detachTransactions(){

        for (Transaction t : getTransactions()) {
            t.setRecurringTransaction(null);
        }
        setTransactions(null);
    }

    public void addTransaction(Transaction transaction) {

        if (transactions == null) {
            transactions = new HashSet<>();
        }
        transactions.add(transaction);
        transaction.setRecurringTransaction(this);
    }

    @Override
    public String toString() {
        return "RecurringTransaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", recurringInterval=" + recurringInterval +
                ", recurringValue=" + recurringValue +
                ", nextOccurrence=" + nextOccurrence +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
