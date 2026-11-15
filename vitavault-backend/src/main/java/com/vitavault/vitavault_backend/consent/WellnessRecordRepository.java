package com.vitavault.vitavault_backend.consent;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WellnessRecordRepository extends JpaRepository<WellnessRecord, Long> {
    List<WellnessRecord> findByUserIdAndCategoryOrderByRecordDateDesc(Long userId, String category);
}