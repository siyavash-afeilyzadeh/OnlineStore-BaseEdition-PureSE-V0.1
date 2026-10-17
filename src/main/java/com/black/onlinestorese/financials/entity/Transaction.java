package com.black.onlinestorese.financials.entity;

import com.black.onlinestorese.financials.entity.enums.TransactionType;

import java.math.BigDecimal;

public class Transaction {

    private TransactionType transactionType;
    private BigDecimal amount;
    private String description;
    private BankAccount bankAccount;
    private Cashier cashier;
}
