package com.example.budget_management_app.session.domain;

import com.example.budget_management_app.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_info")
    private String deviceInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    private DeviceType deviceType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Transient
    private String rawRefreshToken;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.lastUsedAt = Instant.now();
    }

}
