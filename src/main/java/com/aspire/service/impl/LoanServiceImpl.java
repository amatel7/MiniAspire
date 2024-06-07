package com.aspire.service.impl;

import com.aspire.entities.Loan;
import com.aspire.entities.User;
import com.aspire.service.LoanService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class LoanServiceImpl implements LoanService {
    @Override
    public Loan createLoan(User user, BigDecimal amount, int term, LocalDate startDate) {
        return null;
    }
}
