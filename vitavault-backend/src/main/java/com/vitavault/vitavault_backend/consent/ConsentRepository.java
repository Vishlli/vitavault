package com.vitavault.vitavault_backend.consent;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsentRepository extends JpaRepository<Consent, Long> {
    List<Consent> findByUserIdOrderByIdDesc(Long userId);
    List<Consent> findByUserIdAndProviderIdAndCategoryAndStatus(
            Long userId, Long providerId, String category, String status);
}