package com.vitavault.vitavault_backend.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private String account;

    @Column(nullable = false)
    private BigDecimal debit;

    @Column(nullable = false)
    private BigDecimal credit;

    protected LedgerEntry() {}

    public LedgerEntry(Long transactionId, String account, BigDecimal debit, BigDecimal credit) {
        this.transactionId = transactionId;
        this.account = account;
        this.debit = debit;
        this.credit = credit;
    }

    public Long getId() { return id; }
    public Long getTransactionId() { return transactionId; }
    public String getAccount() { return account; }
    public BigDecimal getDebit() { return debit; }
    public BigDecimal getCredit() { return credit; }
}