package com.aspire.controller;

import com.aspire.entities.Loan;
import com.aspire.response.BaseResponse;
import com.aspire.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private LoanService loanService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testApproveLoan_LoanNotFound() {
        when(loanService.getLoanById(anyLong())).thenReturn(null);

        ResponseEntity<BaseResponse> response = adminController.approveLoan(1L);

        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Loan not found", response.getBody().getMessage());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getData());

        verify(loanService, times(1)).getLoanById(anyLong());
        verify(loanService, times(0)).approveLoan(any(Loan.class));
    }

    @Test
    public void testApproveLoan_Success() {
        Loan loan = new Loan();
        loan.setId(1L);

        when(loanService.getLoanById(anyLong())).thenReturn(loan);
        when(loanService.approveLoan(any(Loan.class))).thenReturn(loan);

        ResponseEntity<BaseResponse> response = adminController.approveLoan(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Loan approved successfully", response.getBody().getMessage());
        assertTrue(response.getBody().isSuccess());
        assertEquals(loan, response.getBody().getData());

        verify(loanService, times(1)).getLoanById(anyLong());
        verify(loanService, times(1)).approveLoan(any(Loan.class));
    }
}
