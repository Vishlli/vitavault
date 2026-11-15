package com.vitavault.vitavault_backend.payment;

import com.vitavault.vitavault_backend.wallet.Wallet;
import com.vitavault.vitavault_backend.wallet.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class RefundService {

    private final PaymentTransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerRepository;
    private final RefundRepository refundRepository;
    private final WalletRepository walletRepository;

    public RefundService(PaymentTransactionRepository transactionRepository,
                         LedgerEntryRepository ledgerRepository,
                         RefundRepository refundRepository,
                         WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
        this.refundRepository = refundRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public Refund refund(Long userId, Long transactionId) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wallet not found"));

        PaymentTransaction tx = transactionRepository.findById(transactionId)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));

        if (!"SUCCESS".equals(tx.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Payment is not refundable (status: " + tx.getStatus() + ")");
        }

        Refund refund = refundRepository.save(new Refund(tx.getId(), tx.getAmount(), "COMPLETED"));

        wallet.setBalance(wallet.getBalance().add(tx.getAmount()));
        tx.setStatus("REFUNDED");

        ledgerRepository.save(new LedgerEntry(tx.getId(), "MERCHANT_PAYABLE",
                tx.getAmount(), BigDecimal.ZERO));
        ledgerRepository.save(new LedgerEntry(tx.getId(), "USER_WALLET",
                BigDecimal.ZERO, tx.getAmount()));

        return refund;
    }
}