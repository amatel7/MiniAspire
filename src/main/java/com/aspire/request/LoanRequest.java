package com.aspire.request;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanRequest {
    private BigDecimal amount;
    private int term;
    private LocalDate startDate;
}
