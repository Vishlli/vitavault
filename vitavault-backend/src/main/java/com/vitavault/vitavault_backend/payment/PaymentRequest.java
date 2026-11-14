package com.vitavault.vitavault_backend.payment;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(@NotNull Long serviceId) {
}