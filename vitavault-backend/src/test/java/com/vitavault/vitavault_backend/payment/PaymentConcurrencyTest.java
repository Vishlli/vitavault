package com.vitavault.vitavault_backend.payment;

import com.vitavault.vitavault_backend.user.User;
import com.vitavault.vitavault_backend.user.UserRepository;
import com.vitavault.vitavault_backend.wallet.Wallet;
import com.vitavault.vitavault_backend.wallet.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PaymentConcurrencyTest {

    @Autowired PaymentService paymentService;
    @Autowired UserRepository userRepository;
    @Autowired WalletRepository walletRepository;
    @Autowired PaymentTransactionRepository transactionRepository;
    @Autowired LedgerEntryRepository ledgerRepository;

    @Test
    void tenConcurrentDuplicateRequestsCreateExactlyOneTransaction() throws Exception {
        User user = userRepository.save(
                new User("conc-" + UUID.randomUUID() + "@test.com", "x", "USER"));
        Long userId = user.getId();
        walletRepository.save(new Wallet(userId, new BigDecimal("1000.00"), "INR"));

        String key = UUID.randomUUID().toString();
        int n = 10;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch ready = new CountDownLatch(n);
        CountDownLatch go = new CountDownLatch(1);

        List<Future<PaymentService.PaymentResult>> futures = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                go.await();
                return paymentService.createPayment(userId, key, new PaymentRequest(1L));
            }));
        }
        ready.await();
        go.countDown();

        long created = 0;
        long replayed = 0;
        Long transactionId = null;
        for (Future<PaymentService.PaymentResult> f : futures) {
            PaymentService.PaymentResult result = f.get();
            if (transactionId == null) {
                transactionId = result.transaction().getId();
            }
            assertEquals(transactionId, result.transaction().getId());
            if (result.replayed()) replayed++; else created++;
        }
        pool.shutdown();

        assertEquals(1, created);
        assertEquals(n - 1, replayed);
        assertEquals(1, transactionRepository.findByUserIdOrderByIdDesc(userId).size());
        assertEquals(2, ledgerRepository.findByTransactionId(transactionId).size());
        assertEquals(0, new BigDecimal("901.00").compareTo(
                walletRepository.findById(userId).orElseThrow().getBalance()));
    }
}