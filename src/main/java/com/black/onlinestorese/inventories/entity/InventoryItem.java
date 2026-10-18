package com.black.onlinestorese.inventories.entity;

import com.black.onlinestorese.products.entity.Product;

import java.time.LocalDate;

public class InventoryItem {

    private int quantity;
    private int minimumQuantity;
    private String batchNumber;
    private LocalDate expirationDate;
    private Inventory inventory;
    private Product product;
}
