package com.vitavault.vitavault_backend.consent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ConsentController {

    record RecordRequest(
            @NotBlank @Pattern(regexp = "SLEEP|ACTIVITY") String category,
            @NotNull LocalDate recordDate,
            @NotNull @DecimalMin("0") BigDecimal value) {}

    record GrantRequest(
            @NotNull Long providerId,
            @NotBlank @Pattern(regexp = "SLEEP|ACTIVITY") String category,
            @Min(1) @Max(30) int days) {}

    record ConsentView(Long id, Long providerId, String category, String status,
                       LocalDateTime expiresAt, LocalDateTime revokedAt) {
        static ConsentView from(Consent c) {
            return new ConsentView(c.getId(), c.getProviderId(), c.getCategory(),
                    c.getStatus(), c.getExpiresAt(), c.getRevokedAt());
        }
    }

    record RecordView(String category, LocalDate recordDate, BigDecimal value) {}

    record LogView(Long consentId, Long providerId, String category, String outcome,
                   LocalDateTime accessedAt) {}

    private final ConsentService service;

    public ConsentController(ConsentService service) {
        this.service = service;
    }

    private static Long userId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }

    @PostMapping("/wellness/records")
    @ResponseStatus(HttpStatus.CREATED)
    public RecordView addRecord(@AuthenticationPrincipal Jwt jwt,
                                @Valid @RequestBody RecordRequest r) {
        WellnessRecord saved = service.addRecord(userId(jwt), r.category(), r.recordDate(), r.value());
        return new RecordView(saved.getCategory(), saved.getRecordDate(), saved.getValue());
    }

    @PostMapping("/consents")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsentView grant(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody GrantRequest r) {
        return ConsentView.from(service.grant(userId(jwt), r.providerId(), r.category(), r.days()));
    }

    @GetMapping("/consents")
    public List<ConsentView> list(@AuthenticationPrincipal Jwt jwt) {
        return service.list(userId(jwt)).stream().map(ConsentView::from).toList();
    }

    @PostMapping("/consents/{id}/revoke")
    public ConsentView revoke(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ConsentView.from(service.revoke(userId(jwt), id));
    }

    @GetMapping("/access-logs")
    public List<LogView> logs(@AuthenticationPrincipal Jwt jwt) {
        return service.accessLog(userId(jwt)).stream()
                .map(l -> new LogView(l.getConsentId(), l.getProviderId(), l.getCategory(),
                        l.getOutcome(), l.getAccessedAt()))
                .toList();
    }

    @GetMapping("/provider/data/{userId}")
    public List<RecordView> providerRead(@AuthenticationPrincipal Jwt jwt,
                                         @PathVariable Long userId,
                                         @RequestParam String category) {
        if (!"PROVIDER".equals(jwt.getClaimAsString("role"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Providers only");
        }
        if (!category.equals("SLEEP") && !category.equals("ACTIVITY")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown category");
        }
        return service.readForProvider(userId(jwt), userId, category)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "No valid consent for this data"))
                .stream()
                .map(r -> new RecordView(r.getCategory(), r.getRecordDate(), r.getValue()))
                .toList();
    }
}