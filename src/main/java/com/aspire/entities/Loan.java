package com.aspire.entities;

import com.aspire.enums.LoanStatus;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;
    private BigDecimal pendingAmount;
    private int term;
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Repayment> repayments;
}
