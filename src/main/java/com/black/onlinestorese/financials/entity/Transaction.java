package com.black.onlinestorese.financials.entity;

import com.black.onlinestorese.financials.entity.enums.TransactionType;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private TransactionType transactionType;
    private BigDecimal amount;
    private String description;
    private BankAccount bankAccount;
    private Cashier cashier;

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

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public void setCashier(Cashier cashier) {
        this.cashier = cashier;
    }

    // ==================== No-Arg Constructor ====================

    public Transaction() {
    }

    // ==================== All-Arguments Constructor ====================

    public Transaction(Long id,
                       Long version,
                       boolean deleted,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt,
                       LocalDateTime deletedAt,
                       TransactionType transactionType,
                       BigDecimal amount,
                       String description,
                       BankAccount bankAccount,
                       Cashier cashier) {
        this.id = id;
        this.version = version;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.amount = amount;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.bankAccount = bankAccount;
        this.cashier = cashier;
    }

    // ==================== Builder ====================

    private Transaction(Builder builder) {
        this.id = builder.id;
        this.version = builder.version;
        this.deleted = builder.deleted;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.deletedAt = builder.deletedAt;
        this.transactionType = builder.transactionType;
        this.amount = builder.amount;
        this.description = builder.description;
        this.bankAccount = builder.bankAccount;
        this.cashier = builder.cashier;
    }

    private static class Builder {
        private Long id;
        private Long version;
        private boolean deleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
        private TransactionType transactionType;
        private BigDecimal amount;
        private String description;
        private BankAccount bankAccount;
        private Cashier cashier;

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

        public Builder transactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder bankAccount(BankAccount bankAccount) {
            this.bankAccount = bankAccount;
            return this;
        }

        public Builder cashier(Cashier cashier) {
            this.cashier = cashier;
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
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
        Transaction transaction = (Transaction) o;
        return id != null && id.equals(transaction.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
