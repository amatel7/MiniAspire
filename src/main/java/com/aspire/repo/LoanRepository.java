package com.aspire.repo;

import com.aspire.entities.Loan;
import com.aspire.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    Loan findByIdAndUserId(Long loanId, Long userId);
    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);
}