package com.vitavault.vitavault_backend.payment;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/payments/{id}/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> refund(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        Refund refund = refundService.refund(userId, id);
        return Map.of("refundId", refund.getId(), "paymentId", refund.getTransactionId(),
                "amount", refund.getAmount(), "status", refund.getStatus());
    }
}