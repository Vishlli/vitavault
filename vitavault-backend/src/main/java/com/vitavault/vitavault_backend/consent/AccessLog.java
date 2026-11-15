package com.vitavault.vitavault_backend.consent;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consent_id")
    private Long consentId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String outcome;

    @Column(name = "accessed_at", insertable = false, updatable = false)
    private LocalDateTime accessedAt;

    protected AccessLog() {}

    public AccessLog(Long consentId, Long providerId, Long userId, String category, String outcome) {
        this.consentId = consentId;
        this.providerId = providerId;
        this.userId = userId;
        this.category = category;
        this.outcome = outcome;
    }

    public Long getConsentId() { return consentId; }
    public Long getProviderId() { return providerId; }
    public String getCategory() { return category; }
    public String getOutcome() { return outcome; }
    public LocalDateTime getAccessedAt() { return accessedAt; }
}