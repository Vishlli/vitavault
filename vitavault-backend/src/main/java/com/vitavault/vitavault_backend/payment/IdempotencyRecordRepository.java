package com.vitavault.vitavault_backend.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    @Modifying
    @Query(value = "INSERT INTO idempotency_records (user_id, idempotency_key, request_hash) "
            + "VALUES (:userId, :key, :hash) "
            + "ON CONFLICT (user_id, idempotency_key) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("userId") Long userId,
                       @Param("key") String key,
                       @Param("hash") String hash);

    Optional<IdempotencyRecord> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
}