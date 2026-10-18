package com.black.onlinestorese.inventories.entity;

import com.black.onlinestorese.inventories.entity.enums.InventoryReferenceType;
import com.black.onlinestorese.inventories.entity.enums.InventoryTransactionType;

public class InventoryTransaction {

    private int quantity;
    private InventoryTransactionType transactionType;
    private InventoryReferenceType referenceType;
    private Long referenceId;
    private String reason;
    private InventoryItem inventoryItem;
}