package com.aspire.request;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class RepaymentRequest {
    private BigDecimal amount;
}
