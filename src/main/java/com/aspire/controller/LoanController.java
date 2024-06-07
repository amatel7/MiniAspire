package com.aspire.controller;

import com.aspire.entities.Loan;
import com.aspire.entities.User;
import com.aspire.request.LoanRequest;
import com.aspire.response.BaseResponse;
import com.aspire.service.LoanService;
import com.aspire.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoanService loanService;

    @PostMapping
    public ResponseEntity<BaseResponse> createLoan(@RequestBody LoanRequest loanRequest){
        User user = userService.getAuthenticatedUser();
        Loan loan = loanService.createLoan(user, loanRequest.getAmount(), loanRequest.getTerm(), loanRequest.getStartDate());
        return ResponseEntity.ok(new BaseResponse("Loan created successfully",true,loan));
    }
}
