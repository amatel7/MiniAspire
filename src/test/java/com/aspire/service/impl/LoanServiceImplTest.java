package com.aspire.service.impl;

import com.aspire.entities.Loan;
import com.aspire.entities.Repayment;
import com.aspire.entities.User;
import com.aspire.enums.LoanStatus;
import com.aspire.enums.RepaymentStatus;
import com.aspire.repo.LoanRepository;
import com.aspire.repo.RepaymentRepository;
import com.aspire.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class LoanServiceImplTest {

    @InjectMocks
    private LoanServiceImpl loanService;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private RepaymentRepository repaymentRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateLoan_Success() {
        User user = new User();
        user.setId(1L);
        BigDecimal amount = BigDecimal.valueOf(1000.0);
        int term = 12;
        LocalDate startDate = LocalDate.now().plusDays(1);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setAmount(amount);
        loan.setPendingAmount(amount);
        loan.setTerm(term);
        loan.setStartDate(startDate);
        loan.setStatus(LoanStatus.PENDING);

        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        Loan createdLoan = loanService.createLoan(user, amount, term, startDate);

        assertNotNull(createdLoan);
        assertEquals(user, createdLoan.getUser());
        assertEquals(amount, createdLoan.getAmount());
        assertEquals(term, createdLoan.getTerm());
        assertEquals(startDate, createdLoan.getStartDate());
        assertEquals(LoanStatus.PENDING, createdLoan.getStatus());

        verify(loanRepository, times(1)).save(any(Loan.class));
    }


    @Test
    void testApproveLoan() {
        // Arrange
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setAmount(BigDecimal.valueOf(1000));
        loan.setTerm(10);
        loan.setStartDate(LocalDate.now());
        loan.setStatus(LoanStatus.PENDING);

        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // Act
        Loan approvedLoan = loanService.approveLoan(loan);

        // Assert
        assertNotNull(approvedLoan);
        assertEquals(LoanStatus.APPROVED, approvedLoan.getStatus());

        // Verify repayment schedules
        List<Repayment> repayments = approvedLoan.getRepayments();
        assertEquals(10, repayments.size());

        BigDecimal weeklyAmount = BigDecimal.valueOf(100);
        for (int i = 0; i < 10; i++) {
            Repayment repayment = repayments.get(i);
            assertEquals(loan, repayment.getLoan());
            assertEquals(loan.getStartDate().plusWeeks(i), repayment.getDueDate());
            if (i == 9) {
                assertEquals(BigDecimal.valueOf(100), repayment.getAmount());
            } else {
                assertEquals(weeklyAmount, repayment.getAmount());
            }
            assertEquals(repayment.getAmount(), repayment.getPendingAmount());
            assertEquals(RepaymentStatus.PENDING, repayment.getStatus());
        }

        verify(loanRepository, times(1)).save(loan);
    }

    @Test
    public void testGetLoansByUser_WithStatus() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setStatus(LoanStatus.APPROVED);

        when(loanRepository.findByUserIdAndStatus(anyLong(), any(LoanStatus.class))).thenReturn(Collections.singletonList(loan));

        List<Loan> loans = loanService.getLoansByUser(1L, LoanStatus.APPROVED.name());

        assertNotNull(loans);
        assertEquals(1, loans.size());
        assertEquals(LoanStatus.APPROVED, loans.get(0).getStatus());

        verify(loanRepository, times(1)).findByUserIdAndStatus(anyLong(), any(LoanStatus.class));
    }

    @Test
    public void testGetLoansByUser_WithoutStatus() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setStatus(LoanStatus.APPROVED);

        when(loanRepository.findByUserId(anyLong())).thenReturn(Collections.singletonList(loan));

        List<Loan> loans = loanService.getLoansByUser(1L, null);

        assertNotNull(loans);
        assertEquals(1, loans.size());
        assertEquals(LoanStatus.APPROVED, loans.get(0).getStatus());

        verify(loanRepository, times(1)).findByUserId(anyLong());
    }

    @Test
    public void testAddRepayment_AmountLessThanPending() {
        Loan loan = new Loan();
        loan.setPendingAmount(BigDecimal.valueOf(1000.0));

        Repayment repayment = new Repayment();
        repayment.setLoan(loan);
        repayment.setPendingAmount(BigDecimal.valueOf(500.0));

        BigDecimal amount = BigDecimal.valueOf(400.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            loanService.addRepayment(repayment, amount);
        });

        assertEquals("Repayment amount is less than the pending amount", exception.getMessage());
    }

    @Test
    public void testAddRepayment_Success() {
        Loan loan = new Loan();
        loan.setPendingAmount(BigDecimal.valueOf(1000.0));

        Repayment repayment = new Repayment();
        repayment.setLoan(loan);
        repayment.setPendingAmount(BigDecimal.valueOf(500.0));
        repayment.setDueDate(LocalDate.now());

        BigDecimal amount = BigDecimal.valueOf(600.0);

        when(repaymentRepository.save(any(Repayment.class))).thenReturn(repayment);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        Repayment updatedRepayment = loanService.addRepayment(repayment, amount);

        assertNotNull(updatedRepayment);
        assertEquals(BigDecimal.ZERO, updatedRepayment.getPendingAmount());
        assertEquals(RepaymentStatus.PAID, updatedRepayment.getStatus());

        verify(repaymentRepository, times(1)).save(any(Repayment.class));
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    public void testGetRepaymentById_Success() {
        Repayment repayment = new Repayment();
        repayment.setId(1L);

        when(repaymentRepository.findById(anyLong())).thenReturn(Optional.of(repayment));

        Repayment foundRepayment = loanService.getRepaymentById(1L);

        assertNotNull(foundRepayment);
        assertEquals(1L, foundRepayment.getId());

        verify(repaymentRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testGetRepaymentById_NotFound() {
        when(repaymentRepository.findById(anyLong())).thenReturn(Optional.empty());

        Repayment repayment = loanService.getRepaymentById(1L);

        assertNull(repayment);

        verify(repaymentRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testGetLoanById_Success() {
        Loan loan = new Loan();
        loan.setId(1L);

        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));

        Loan foundLoan = loanService.getLoanById(1L);

        assertNotNull(foundLoan);
        assertEquals(1L, foundLoan.getId());

        verify(loanRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testGetLoanById_NotFound() {
        when(loanRepository.findById(anyLong())).thenReturn(Optional.empty());

        Loan loan = loanService.getLoanById(1L);

        assertNull(loan);

        verify(loanRepository, times(1)).findById(anyLong());
    }
}
