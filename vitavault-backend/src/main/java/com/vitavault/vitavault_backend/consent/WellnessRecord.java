package com.vitavault.vitavault_backend.consent;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "wellness_records")
public class WellnessRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String category;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "metric_value", nullable = false)
    private BigDecimal value;

    protected WellnessRecord() {}

    public WellnessRecord(Long userId, String category, LocalDate recordDate, BigDecimal value) {
        this.userId = userId;
        this.category = category;
        this.recordDate = recordDate;
        this.value = value;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getCategory() { return category; }
    public LocalDate getRecordDate() { return recordDate; }
    public BigDecimal getValue() { return value; }
}