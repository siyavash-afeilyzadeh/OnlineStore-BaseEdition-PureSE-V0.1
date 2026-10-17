package com.black.onlinestorese.financials.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class CardPayment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private String cardLastDigits;
    private String trackingCode;
}
