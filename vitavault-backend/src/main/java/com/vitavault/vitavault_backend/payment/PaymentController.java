package com.vitavault.vitavault_backend.payment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/services")
    public List<ServiceItem> services() {
        return paymentService.activeServices();
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody PaymentRequest request) {
        if (key.isBlank() || key.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Idempotency-Key must be 1-100 characters");
        }
        Long userId = Long.parseLong(jwt.getSubject());
        PaymentService.PaymentResult result = paymentService.createPayment(userId, key, request);
        HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(PaymentResponse.from(result.transaction()));
    }

    @GetMapping("/payments")
    public List<PaymentResponse> list(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        return paymentService.listForUser(userId).stream().map(PaymentResponse::from).toList();
    }

    @GetMapping("/payments/{id}")
    public Map<String, Object> get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        PaymentTransaction tx = paymentService.getForUser(userId, id);
        List<Map<String, Object>> ledger = paymentService.ledgerFor(tx.getId()).stream()
                .map(e -> Map.<String, Object>of("account", e.getAccount(),
                        "debit", e.getDebit(), "credit", e.getCredit()))
                .toList();
        return Map.of("payment", PaymentResponse.from(tx), "ledger", ledger);
    }
}