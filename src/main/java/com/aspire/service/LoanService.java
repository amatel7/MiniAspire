package com.aspire.service;


import com.aspire.entities.Loan;
import com.aspire.entities.User;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface LoanService {
    public Loan createLoan(User user, BigDecimal amount, int term , LocalDate startDate);
}
