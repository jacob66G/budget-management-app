package com.example.budget_management_app.account.domain;

import com.example.budget_management_app.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalExpense = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportedCurrency currency;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    private String description;

    @Column(name = "monthly_budget")
    private BigDecimal monthlyBudget;

    @Column(name = "weekly_budget")
    private BigDecimal weeklyBudget;

    @Column(name = "alter_treshold")
    private Double alertThreshold;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
