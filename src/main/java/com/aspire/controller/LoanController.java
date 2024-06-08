package com.aspire.controller;

import com.aspire.entities.Loan;
import com.aspire.entities.Repayment;
import com.aspire.entities.User;
import com.aspire.enums.LoanStatus;
import com.aspire.enums.RepaymentStatus;
import com.aspire.request.LoanRequest;
import com.aspire.request.RepaymentRequest;
import com.aspire.response.BaseResponse;
import com.aspire.service.LoanService;
import com.aspire.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoanService loanService;

    @PostMapping
    public ResponseEntity<BaseResponse> createLoan(@RequestHeader Map<String, String> headers, @RequestBody LoanRequest loanRequest){
        if(loanRequest.getAmount() == null ||  loanRequest.getAmount().compareTo(BigDecimal.valueOf(0.0)) <= 0){
            return ResponseEntity.badRequest().body(new BaseResponse("Amount should be greater than 0", false, null));
        }
        if( loanRequest.getTerm() <= 0){
            return ResponseEntity.badRequest().body(new BaseResponse("Term should be greater than 0", false, null));
        }
        if(loanRequest.getStartDate() == null){
            return ResponseEntity.badRequest().body(new BaseResponse("Start date is required", false, null));
        }
        if(loanRequest.getStartDate().isBefore(LocalDate.now().atStartOfDay().toLocalDate())){
            return ResponseEntity.badRequest().body(new BaseResponse("Start date should not be in past", false, null));
        }

        User user = userService.getUserByToken(headers.get("x-user-token"));
        Loan loan = loanService.createLoan(user, loanRequest.getAmount(), loanRequest.getTerm(), loanRequest.getStartDate());
        return ResponseEntity.ok(new BaseResponse("Loan created successfully",true,loan));
    }

    @GetMapping
    public ResponseEntity<BaseResponse> getLoans(@RequestHeader Map<String, String> headers, @RequestParam(required = false) String status){
        User user = userService.getUserByToken(headers.get("x-user-token"));
        return ResponseEntity.ok(new BaseResponse("Success",true,loanService.getLoansByUser(user.getId(), status)));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<BaseResponse> getLoans(@RequestHeader Map<String, String> headers, @PathVariable Long loanId){
        User user = userService.getUserByToken(headers.get("x-user-token"));
        Loan loan = loanService.getLoanById(loanId);
        if(loan == null){
            return ResponseEntity.badRequest().body(new BaseResponse("Loan not found", false, null));
        }
        if(!user.getId().equals(loan.getUser().getId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new BaseResponse("Loan does not belong to user", false, null));
        }
        return ResponseEntity.ok(new BaseResponse("Success",true,loan));
    }

    @PostMapping("/repayments/{repaymentId}")
    public ResponseEntity<BaseResponse> addRepayment(@PathVariable Long repaymentId, @RequestBody RepaymentRequest repaymentRequest, @RequestHeader Map<String, String> headers) {

        if(repaymentRequest.getAmount().compareTo(BigDecimal.valueOf(0.0)) <= 0){
            return ResponseEntity.badRequest().body(new BaseResponse("Amount should be greater than 0", false, null));
        }

        Repayment repayment = loanService.getRepaymentById(repaymentId);
        if(repayment == null){
            return ResponseEntity.badRequest().body(new BaseResponse("Repayment not found", false, null));
        }
        User user = userService.getUserByToken(headers.get("x-user-token"));
        if(!user.getId().equals(repayment.getLoan().getUser().getId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new BaseResponse("Repayment does not belong to user", false, null));
        }

        if(repayment.getStatus().equals(RepaymentStatus.PAID)){
            return ResponseEntity.badRequest().body(new BaseResponse("Repayment already paid", false, null));
        }

        if(repayment.getLoan().getPendingAmount().compareTo(repaymentRequest.getAmount()) < 0){
            return ResponseEntity.badRequest().body(new BaseResponse("Amount is greater than total pending amount", false, null));
        }
        try{
            Repayment repaymentPaid = loanService.addRepayment(repayment, repaymentRequest.getAmount());
        }
        catch (IllegalArgumentException ex){
            return ResponseEntity.badRequest().body(new BaseResponse(ex.getMessage(), false, null));
        }
        return ResponseEntity.ok(new BaseResponse("Repayment added successfully", true, repayment));
    }



}
