package com.example.budget_management_app.user.domain;

import com.example.budget_management_app.account.domain.Account;
import com.example.budget_management_app.category.domain.Category;
import com.example.budget_management_app.session.domain.UserSession;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "mfa_enabled")
    private boolean mfaEnabled;

    @Column(name = "two_factor_secret")
    private String secret;

    @Column(name = "temp_two_factor_secret")
    private String tempSecret;

    @Column(name = "request_close_at")
    private Instant requestCloseAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSession> sessions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Account> accounts = new ArrayList<>();

    public void addSession(UserSession session) {
        sessions.add(session);
        session.setUser(this);
    }

    public void removeSession(UserSession session) {
        sessions.remove(session);
        session.setUser(null);
    }

    public void addCategory(Category category) {
        categories.add(category);
        category.setUser(this);
    }

    public void addAccount(Account account) {
        accounts.add(account);
        account.setUser(this);
    }

}
