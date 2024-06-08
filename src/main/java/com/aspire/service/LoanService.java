package com.aspire.service;


import com.aspire.entities.Loan;
import com.aspire.entities.Repayment;
import com.aspire.entities.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LoanService {
    public Loan createLoan(User user, BigDecimal amount, int term , LocalDate startDate);
    public Loan approveLoan(Loan loan);
    public List<Loan> getLoansByUser(Long userId, String status);
    public Repayment addRepayment(Repayment repayment, BigDecimal amount);
    public Repayment getRepaymentById(Long repaymentId);
    public Loan getLoanById(Long loanId);
}
