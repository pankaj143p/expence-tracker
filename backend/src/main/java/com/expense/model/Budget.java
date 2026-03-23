package com.expense.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
@Data
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String category; // null = overall budget

    @Column(nullable = false)
    private BigDecimal dailyLimit;

    @Column(nullable = false)
    private BigDecimal monthlyLimit;

    private int month; // 1-12

    private int year;
}
