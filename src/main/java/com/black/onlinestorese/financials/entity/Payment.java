package com.black.onlinestorese.financials.entity;

import com.black.onlinestorese.financials.entity.enums.PaymentStatus;
import com.black.onlinestorese.financials.entity.enums.PaymentType;

import java.math.BigDecimal;

public class Payment {

    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentType type;
    private Long orderId;
}
