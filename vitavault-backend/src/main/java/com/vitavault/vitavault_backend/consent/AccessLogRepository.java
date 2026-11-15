package com.vitavault.vitavault_backend.consent;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    List<AccessLog> findByUserIdOrderByIdDesc(Long userId);
}