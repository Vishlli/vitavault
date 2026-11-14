package com.vitavault.vitavault_backend.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(Long id, Long serviceId, BigDecimal amount,
                              String currency, String status, LocalDateTime createdAt) {

    public static PaymentResponse from(PaymentTransaction t) {
        return new PaymentResponse(t.getId(), t.getServiceId(), t.getAmount(),
                t.getCurrency(), t.getStatus(), t.getCreatedAt());
    }
}