package com.aspire.controller;

import com.aspire.entities.Loan;
import com.aspire.entities.Repayment;
import com.aspire.entities.User;
import com.aspire.enums.RepaymentStatus;
import com.aspire.request.LoanRequest;
import com.aspire.request.RepaymentRequest;
import com.aspire.response.BaseResponse;
import com.aspire.service.LoanService;
import com.aspire.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class LoanControllerTest {

    @InjectMocks
    private LoanController loanController;

    @Mock
    private UserService userService;

    @Mock
    private LoanService loanService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateLoan_InvalidAmount() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setAmount(BigDecimal.valueOf(0.0));
        loanRequest.setTerm(12);
        loanRequest.setStartDate(LocalDate.now().plusDays(1));

        ResponseEntity<BaseResponse> response = loanController.createLoan(headers, loanRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Amount should be greater than 0", response.getBody().getMessage());
    }

    @Test
    public void testCreateLoan_InvalidTerm() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setAmount(BigDecimal.valueOf(1000.0));
        loanRequest.setTerm(0);
        loanRequest.setStartDate(LocalDate.now().plusDays(1));

        ResponseEntity<BaseResponse> response = loanController.createLoan(headers, loanRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Term should be greater than 0", response.getBody().getMessage());
    }

    @Test
    public void testCreateLoan_InvalidStartDate() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setAmount(BigDecimal.valueOf(1000.0));
        loanRequest.setTerm(12);
        loanRequest.setStartDate(LocalDate.now().minusDays(1));

        ResponseEntity<BaseResponse> response = loanController.createLoan(headers, loanRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Start date should not be in past", response.getBody().getMessage());
    }

    @Test
    public void testCreateLoan_Success() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setAmount(BigDecimal.valueOf(1000.0));
        loanRequest.setTerm(12);
        loanRequest.setStartDate(LocalDate.now().plusDays(1));

        User user = new User();
        user.setId(1L);

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setUser(user);

        when(userService.getUserByToken("valid-token")).thenReturn(user);
        when(loanService.createLoan(user, loanRequest.getAmount(), loanRequest.getTerm(), loanRequest.getStartDate())).thenReturn(loan);

        ResponseEntity<BaseResponse> response = loanController.createLoan(headers, loanRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Loan created successfully", response.getBody().getMessage());
        assertEquals(loan, response.getBody().getData());
    }

    @Test
    public void testGetLoans_Success() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        User user = new User();
        user.setId(1L);

        when(userService.getUserByToken("valid-token")).thenReturn(user);
        when(loanService.getLoansByUser(1L, null)).thenReturn(Collections.emptyList());

        ResponseEntity<BaseResponse> response = loanController.getLoans(headers, (String) null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(Collections.emptyList(), response.getBody().getData());
    }

    @Test
    public void testGetLoan_LoanNotFound() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        User user = new User();
        user.setId(1L);

        when(userService.getUserByToken("valid-token")).thenReturn(user);
        when(loanService.getLoanById(anyLong())).thenReturn(null);

        ResponseEntity<BaseResponse> response = loanController.getLoans(headers, 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Loan not found", response.getBody().getMessage());
        assertEquals(false, response.getBody().isSuccess());
    }

    @Test
    public void testGetLoan_LoanDoesNotBelongToUser() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        User user = new User();
        user.setId(1L);

        Loan loan = new Loan();
        loan.setId(1L);
        User differentUser = new User();
        differentUser.setId(2L);
        loan.setUser(differentUser);

        when(userService.getUserByToken("valid-token")).thenReturn(user);
        when(loanService.getLoanById(anyLong())).thenReturn(loan);

        ResponseEntity<BaseResponse> response = loanController.getLoans(headers, 1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Loan does not belong to user", response.getBody().getMessage());
        assertEquals(false, response.getBody().isSuccess());
    }

    @Test
    public void testGetLoan_Success() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        User user = new User();
        user.setId(1L);

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setUser(user);

        when(userService.getUserByToken("valid-token")).thenReturn(user);
        when(loanService.getLoanById(anyLong())).thenReturn(loan);

        ResponseEntity<BaseResponse> response = loanController.getLoans(headers, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals(loan, response.getBody().getData());
    }

    @Test
    public void testAddRepayment_AmountLessThanZero() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setAmount(BigDecimal.valueOf(-1.0));

        ResponseEntity<BaseResponse> response = loanController.addRepayment(1L, repaymentRequest, headers);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Amount should be greater than 0", response.getBody().getMessage());
    }

    @Test
    public void testAddRepayment_RepaymentNotFound() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setAmount(BigDecimal.valueOf(100.0));

        when(loanService.getRepaymentById(anyLong())).thenReturn(null);

        ResponseEntity<BaseResponse> response = loanController.addRepayment(1L, repaymentRequest, headers);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Repayment not found", response.getBody().getMessage());
    }

    @Test
    public void testAddRepayment_RepaymentDoesNotBelongToUser() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        User user = new User();
        user.setId(1L);

        User differentUser = new User();
        differentUser.setId(2L);

        Loan loan = new Loan();
        loan.setUser(differentUser);

        Repayment repayment = new Repayment();
        repayment.setLoan(loan);

        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setAmount(BigDecimal.valueOf(100.0));

        when(userService.getUserByToken("valid-token")).thenReturn(user);
        when(loanService.getRepaymentById(anyLong())).thenReturn(repayment);

        ResponseEntity<BaseResponse> response = loanController.addRepayment(1L, repaymentRequest, headers);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Repayment does not belong to user", response.getBody().getMessage());
    }

    @Test
    public void testAddRepayment_RepaymentAlreadyPaid() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        User user = new User();
        user.setId(1L);

        Loan loan = new Loan();
        loan.setUser(user);

        Repayment repayment = new Repayment();
        repayment.setLoan(loan);
        repayment.setStatus(RepaymentStatus.PAID);

        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setAmount(BigDecimal.valueOf(100.0));

        when(userService.getUserByToken("valid-token")).thenReturn(user);
        when(loanService.getRepaymentById(anyLong())).thenReturn(repayment);

        ResponseEntity<BaseResponse> response = loanController.addRepayment(1L, repaymentRequest, headers);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Repayment already paid", response.getBody().getMessage());
    }

    @Test
    public void testAddRepayment_AmountGreaterThanPendingAmount() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        User user = new User();
        user.setId(1L);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setPendingAmount(BigDecimal.valueOf(50.0));

        Repayment repayment = new Repayment();
        repayment.setLoan(loan);
        repayment.setStatus(RepaymentStatus.PENDING);

        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setAmount(BigDecimal.valueOf(100.0));

        when(userService.getUserByToken("valid-token")).thenReturn(user);
        when(loanService.getRepaymentById(anyLong())).thenReturn(repayment);

        ResponseEntity<BaseResponse> response = loanController.addRepayment(1L, repaymentRequest, headers);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Amount is greater than total pending amount", response.getBody().getMessage());
    }

    @Test
    public void testAddRepayment_Success() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-user-token", "valid-token");

        User user = new User();
        user.setId(1L);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setPendingAmount(BigDecimal.valueOf(100.0));

        Repayment repayment = new Repayment();
        repayment.setLoan(loan);
        repayment.setStatus(RepaymentStatus.PENDING);

        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setAmount(BigDecimal.valueOf(50.0));

        when(userService.getUserByToken("valid-token")).thenReturn(user);
        when(loanService.getRepaymentById(anyLong())).thenReturn(repayment);
        when(loanService.addRepayment(repayment, repaymentRequest.getAmount())).thenReturn(repayment);

        ResponseEntity<BaseResponse> response = loanController.addRepayment(1L, repaymentRequest, headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Repayment added successfully", response.getBody().getMessage());
        assertEquals(repayment, response.getBody().getData());
    }
}
