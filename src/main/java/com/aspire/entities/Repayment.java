package com.aspire.entities;

import com.aspire.enums.RepaymentStatus;
import javax.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Repayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dueDate;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private RepaymentStatus status;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;
}
