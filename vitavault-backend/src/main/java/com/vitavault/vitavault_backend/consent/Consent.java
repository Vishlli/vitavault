package com.vitavault.vitavault_backend.consent;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consents")
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    protected Consent() {}

    public Consent(Long userId, Long providerId, String category, String status,
                   LocalDateTime expiresAt) {
        this.userId = userId;
        this.providerId = providerId;
        this.category = category;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getProviderId() { return providerId; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setStatus(String status) { this.status = status; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
}