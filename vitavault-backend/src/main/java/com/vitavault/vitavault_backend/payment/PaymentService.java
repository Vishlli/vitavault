package com.vitavault.vitavault_backend.payment;

import com.vitavault.vitavault_backend.wallet.Wallet;
import com.vitavault.vitavault_backend.wallet.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Service
public class PaymentService {

    public record PaymentResult(PaymentTransaction transaction, boolean replayed) {}

    private final IdempotencyRecordRepository idempotencyRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerRepository;
    private final ServiceItemRepository serviceRepository;
    private final WalletRepository walletRepository;

    public PaymentService(IdempotencyRecordRepository idempotencyRepository,
                          PaymentTransactionRepository transactionRepository,
                          LedgerEntryRepository ledgerRepository,
                          ServiceItemRepository serviceRepository,
                          WalletRepository walletRepository) {
        this.idempotencyRepository = idempotencyRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
        this.serviceRepository = serviceRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public PaymentResult createPayment(Long userId, String key, PaymentRequest request) {
        String hash = sha256("serviceId=" + request.serviceId());

        int inserted = idempotencyRepository.insertIfAbsent(userId, key, hash);
        IdempotencyRecord record = idempotencyRepository
                .findByUserIdAndIdempotencyKey(userId, key)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Idempotency record missing"));

        if (inserted == 0) {
            if (!record.getRequestHash().equals(hash)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Idempotency-Key was already used with a different request");
            }
            if (record.getTransactionId() == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A request with this key is still being processed");
            }
            PaymentTransaction existing = transactionRepository.findById(record.getTransactionId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Original transaction missing"));
            return new PaymentResult(existing, true);
        }

        ServiceItem service = serviceRepository.findById(request.serviceId())
                .filter(ServiceItem::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Service not found"));

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wallet not found"));

        if (wallet.getBalance().compareTo(service.getPrice()) < 0) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "Insufficient wallet balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(service.getPrice()));

        PaymentTransaction tx = transactionRepository.save(new PaymentTransaction(
                userId, service.getId(), service.getPrice(), service.getCurrency(), "SUCCESS"));

        ledgerRepository.save(new LedgerEntry(tx.getId(), "USER_WALLET",
                service.getPrice(), BigDecimal.ZERO));
        ledgerRepository.save(new LedgerEntry(tx.getId(), "MERCHANT_PAYABLE",
                BigDecimal.ZERO, service.getPrice()));

        record.setTransactionId(tx.getId());
        return new PaymentResult(tx, false);
    }

    @Transactional(readOnly = true)
    public List<PaymentTransaction> listForUser(Long userId) {
        return transactionRepository.findByUserIdOrderByIdDesc(userId);
    }

    @Transactional(readOnly = true)
    public PaymentTransaction getForUser(Long userId, Long id) {
        return transactionRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> ledgerFor(Long transactionId) {
        return ledgerRepository.findByTransactionId(transactionId);
    }

    @Transactional(readOnly = true)
    public List<ServiceItem> activeServices() {
        return serviceRepository.findByActiveTrue();
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}