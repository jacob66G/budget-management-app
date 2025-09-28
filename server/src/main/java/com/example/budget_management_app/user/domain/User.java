package com.example.budget_management_app.user.domain;

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

    @Column(name = "email_last_changed", nullable = false)
    private Instant emailLastChanged;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_expiration")
    private Instant verificationCodeExpiresAt;

    @Column(name = "last_verification_sent_at")
    private Instant lastVerificationSentAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "mfa_enabled")
    private boolean mfaEnabled;

    @Column(name = "two_factor_secret")
    private String secret;

    @Column(name = "temp_two_factor_secret")
    private String tempSecret;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSession> sessions = new ArrayList<>();

    public void addSession(UserSession session) {
        sessions.add(session);
        session.setUser(this);
    }

    public void removeSession(UserSession session) {
        sessions.remove(session);
        session.setUser(null);
    }
}
