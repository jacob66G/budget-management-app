package com.example.budget_management_app.category.domain;

import com.example.budget_management_app.transaction.domain.Transaction;
import com.example.budget_management_app.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString(exclude = "transactions")
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoryType type;

    @Column(name = "icon_key")
    private String iconKey;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Transaction> transactions;
}
