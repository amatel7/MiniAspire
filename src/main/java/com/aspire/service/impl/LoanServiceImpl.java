package com.aspire.service.impl;

import com.aspire.entities.Loan;
import com.aspire.entities.Repayment;
import com.aspire.entities.User;
import com.aspire.enums.LoanStatus;
import com.aspire.enums.RepaymentStatus;
import com.aspire.repo.LoanRepository;
import com.aspire.repo.RepaymentRepository;
import com.aspire.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanServiceImpl implements LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private RepaymentRepository repaymentRepository;

    @Override
    public Loan createLoan(User user, BigDecimal amount, int term, LocalDate startDate) {
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setAmount(amount);
        loan.setPendingAmount(amount);
        loan.setTerm(term);
        loan.setStartDate(startDate);
        loan.setStatus(LoanStatus.PENDING);
        return loanRepository.save(loan);
    }

    @Override
    public Loan approveLoan(Loan loan) {

        loan.setStatus(LoanStatus.APPROVED);

        BigDecimal weeklyAmount = loan.getAmount().divide(BigDecimal.valueOf(loan.getTerm()), RoundingMode.HALF_UP);
        List<Repayment> repayments = new ArrayList<>();
        for (int i = 0; i < loan.getTerm(); i++) {
            Repayment repayment = new Repayment();
            repayment.setLoan(loan);
            repayment.setDueDate(loan.getStartDate().plusWeeks(i));
            repayment.setAmount(i == loan.getTerm() - 1 ? loan.getAmount().subtract(weeklyAmount.multiply(BigDecimal.valueOf(i))) : weeklyAmount);
            repayment.setPendingAmount(repayment.getAmount());
            repayment.setStatus(RepaymentStatus.PENDING);
            repayments.add(repayment);
        }
        loan.setRepayments(repayments);
        return loanRepository.save(loan);
    }

    @Override
    public List<Loan> getLoansByUser(Long userId, String status) {
        if(status != null && Arrays.asList(LoanStatus.values()).stream().anyMatch(s -> s.name().equals(status))){
            return loanRepository.findByUserIdAndStatus(userId, LoanStatus.valueOf(status));
        }
        return loanRepository.findByUserId(userId);
    }

    @Transactional
    @Override
    public Repayment addRepayment(Repayment repayment, BigDecimal amount) {

        if (amount.compareTo(repayment.getPendingAmount()) < 0) {
            throw new IllegalArgumentException("Repayment amount is less than the pending amount");
        }
        Loan loan = repayment.getLoan();

        BigDecimal extraAmount = amount.subtract(repayment.getPendingAmount());

        loan.setPendingAmount(loan.getPendingAmount().subtract(repayment.getPendingAmount()));

        repayment.setPendingAmount(BigDecimal.ZERO);
        repayment.setStatus(RepaymentStatus.PAID);
        repaymentRepository.save(repayment);




        // Apply the extra amount to future pending repayments

        List<Repayment> futureRepayments;
        if(loan.getRepayments() != null) {
            futureRepayments = loan.getRepayments().stream()
                    .filter(r -> r.getStatus().equals(RepaymentStatus.PENDING) && r.getDueDate().isAfter(repayment.getDueDate()))
                    .sorted(Comparator.comparing(Repayment::getDueDate))
                    .collect(Collectors.toList());

            for (Repayment futureRepayment : futureRepayments) {
                if (extraAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                if (extraAmount.compareTo(futureRepayment.getPendingAmount()) >= 0) {
                    loan.setPendingAmount(loan.getPendingAmount().subtract(futureRepayment.getPendingAmount()));
                    extraAmount = extraAmount.subtract(futureRepayment.getPendingAmount());
                    futureRepayment.setPendingAmount(BigDecimal.ZERO);
                    futureRepayment.setStatus(RepaymentStatus.PAID);
                } else {
                    loan.setPendingAmount(loan.getPendingAmount().subtract(extraAmount));
                    futureRepayment.setPendingAmount(futureRepayment.getPendingAmount().subtract(extraAmount));
                    extraAmount = BigDecimal.ZERO;
                }
                repaymentRepository.save(futureRepayment);
            }
            // Check if all repayments are paid
            boolean allPaid = loan.getRepayments().stream()
                    .allMatch(r -> r.getStatus() == RepaymentStatus.PAID);
            if (allPaid) {
                loan.setStatus(LoanStatus.PAID);
            }
        }
        loanRepository.save(loan);
        return repayment;
    }

    @Override
    public Repayment getRepaymentById(Long repaymentId) {
        return repaymentRepository.findById(repaymentId).orElse(null);
    }

    @Override
    public Loan getLoanById(Long loanId) {
        return loanRepository.findById(loanId).orElse(null);
    }


}
