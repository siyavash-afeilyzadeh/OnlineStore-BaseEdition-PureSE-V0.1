package com.black.onlinestorese.inventories.entity;

import com.black.onlinestorese.inventories.entity.enums.InventoryReferenceType;
import com.black.onlinestorese.inventories.entity.enums.InventoryTransactionType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class InventoryTransaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private int quantity;
    private InventoryTransactionType transactionType;
    private InventoryReferenceType referenceType;
    private Long referenceId;
    private String reason;
    private InventoryItem inventoryItem;

    // ==================== Getters & Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public InventoryTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(InventoryTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public InventoryReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(InventoryReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public InventoryItem getInventoryItem() {
        return inventoryItem;
    }

    public void setInventoryItem(InventoryItem inventoryItem) {
        this.inventoryItem = inventoryItem;
    }

    // ==================== No-Arg Constructor ====================

    public InventoryTransaction() {

    }

    // ==================== All-Arguments Constructor ====================

    public InventoryTransaction(Long id,
                                Long version,
                                boolean deleted,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt,
                                LocalDateTime deletedAt,
                                int quantity,
                                InventoryTransactionType transactionType,
                                InventoryReferenceType referenceType,
                                Long referenceId,
                                String reason,
                                InventoryItem inventoryItem) {
        this.id = id;
        this.version = version;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.quantity = quantity;
        this.transactionType = transactionType;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.reason = reason;
        this.inventoryItem = inventoryItem;
    }

    // ==================== Builder ====================

    private InventoryTransaction(Builder builder) {
        this.id = builder.id;
        this.version = builder.version;
        this.deleted = builder.deleted;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.deletedAt = builder.deletedAt;
        this.quantity = builder.quantity;
        this.transactionType = builder.transactionType;
        this.referenceType = builder.referenceType;
        this.referenceId = builder.referenceId;
        this.reason = builder.reason;
        this.inventoryItem = builder.inventoryItem;
    }

    private static class Builder {
        private Long id;
        private Long version;
        private boolean deleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
        private int quantity;
        private InventoryTransactionType transactionType;
        private InventoryReferenceType referenceType;
        private Long referenceId;
        private String reason;
        private InventoryItem inventoryItem;

        public Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public Builder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder deletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder transactionType(InventoryTransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public Builder referenceType(InventoryReferenceType referenceType) {
            this.referenceType = referenceType;
            return this;
        }

        public Builder referenceId(Long referenceId) {
            this.referenceId = referenceId;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder inventoryItem(InventoryItem inventoryItem) {
            this.inventoryItem = inventoryItem;
            return this;
        }

        public InventoryTransaction build() {
            return new InventoryTransaction(this);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    // ==================== Lifecycle Hooks ====================

    public void onCreated() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deleted = false;
        if (this.version == null) {
            this.version = 0L;
        }
    }

    public void onUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    public void onDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // ==================== Equals & HashCode ====================

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InventoryTransaction inventoryTransaction = (InventoryTransaction) o;
        return id != null && id.equals(inventoryTransaction.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}