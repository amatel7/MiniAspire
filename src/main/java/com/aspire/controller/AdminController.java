package com.aspire.controller;

import com.aspire.entities.Loan;
import com.aspire.response.BaseResponse;
import com.aspire.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {


    @Autowired
    private LoanService loanService;

    @PostMapping("loans/{loanId}/approve")
    public ResponseEntity<BaseResponse> approveLoan(@PathVariable Long loanId) {
        Loan loan = loanService.getLoanById(loanId);
        if(loan == null){
            return ResponseEntity.badRequest().body(new BaseResponse("Loan not found", false, null));
        }
        return ResponseEntity.ok(new BaseResponse("Loan approved successfully", true, loanService.approveLoan(loan)));
    }
}
