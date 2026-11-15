package com.vitavault.vitavault_backend.consent;

import com.vitavault.vitavault_backend.user.User;
import com.vitavault.vitavault_backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsentService {

    private final ConsentRepository consentRepository;
    private final AccessLogRepository accessLogRepository;
    private final WellnessRecordRepository recordRepository;
    private final UserRepository userRepository;

    public ConsentService(ConsentRepository consentRepository,
                          AccessLogRepository accessLogRepository,
                          WellnessRecordRepository recordRepository,
                          UserRepository userRepository) {
        this.consentRepository = consentRepository;
        this.accessLogRepository = accessLogRepository;
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WellnessRecord addRecord(Long userId, String category, LocalDate date, BigDecimal value) {
        return recordRepository.save(new WellnessRecord(userId, category, date, value));
    }

    @Transactional
    public Consent grant(Long userId, Long providerId, String category, int days) {
        User provider = userRepository.findById(providerId)
                .filter(u -> "PROVIDER".equals(u.getRole()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Provider not found"));
        return consentRepository.save(new Consent(userId, provider.getId(), category,
                "ACTIVE", LocalDateTime.now().plusDays(days)));
    }

    @Transactional(readOnly = true)
    public List<Consent> list(Long userId) {
        return consentRepository.findByUserIdOrderByIdDesc(userId);
    }

    @Transactional
    public Consent revoke(Long userId, Long consentId) {
        Consent consent = consentRepository.findById(consentId)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Consent not found"));
        if ("ACTIVE".equals(consent.getStatus())) {
            consent.setStatus("REVOKED");
            consent.setRevokedAt(LocalDateTime.now());
        }
        return consent;
    }

    // Returns empty when access is denied. The denial is logged and committed,
    // because this method does not throw; the controller turns empty into a 403.
    @Transactional
    public Optional<List<WellnessRecord>> readForProvider(Long providerId, Long userId,
                                                          String category) {
        LocalDateTime now = LocalDateTime.now();
        Optional<Consent> valid = consentRepository
                .findByUserIdAndProviderIdAndCategoryAndStatus(userId, providerId, category, "ACTIVE")
                .stream()
                .filter(c -> c.getExpiresAt().isAfter(now))
                .findFirst();

        accessLogRepository.save(new AccessLog(valid.map(Consent::getId).orElse(null),
                providerId, userId, category, valid.isPresent() ? "ALLOWED" : "DENIED"));

        return valid.map(c -> recordRepository
                .findByUserIdAndCategoryOrderByRecordDateDesc(userId, category));
    }

    @Transactional(readOnly = true)
    public List<AccessLog> accessLog(Long userId) {
        return accessLogRepository.findByUserIdOrderByIdDesc(userId);
    }
}