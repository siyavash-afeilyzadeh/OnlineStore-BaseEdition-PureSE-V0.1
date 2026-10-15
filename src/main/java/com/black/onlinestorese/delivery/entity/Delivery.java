package com.black.onlinestorese.delivery.entity;

import com.black.onlinestorese.delivery.entity.enums.DeliveryStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Delivery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private String address;
    private DeliveryStatus status;
    private String trackingNumber;
    private LocalDateTime sendDate;
    private LocalDateTime receiveDate;
    private String deliveryNote;
    private Courier courier;
}
