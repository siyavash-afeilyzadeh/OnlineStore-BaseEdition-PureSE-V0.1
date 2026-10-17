package com.black.onlinestorese.financials.entity;

import com.black.onlinestorese.delivery.entity.Delivery;
import com.black.onlinestorese.delivery.entity.enums.DeliveryStatus;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankAccount implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private String accountNumber;
    private String ownerName;
    private BigDecimal balance;
    private Bank bank;

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

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    // ==================== No-Arg Constructor ====================

    public BankAccount() {
    }

    // ==================== All-Arguments Constructor ====================

    public BankAccount(Long id,
                       Long version,
                       boolean deleted,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt,
                       LocalDateTime deletedAt,
                       String accountNumber,
                       String ownerName,
                       BigDecimal balance,
                       Bank bank) {
        this.id = id;
        this.version = version;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
        this.bank = bank;
    }

    // ==================== Builder ====================

    private BankAccount(Builder builder) {
        this.id = builder.id;
        this.version = builder.version;
        this.deleted = builder.deleted;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.deletedAt = builder.deletedAt;
        this.accountNumber = builder.accountNumber;
        this.ownerName = builder.ownerName;
        this.balance = builder.balance;
        this.bank = builder.bank;
    }

    private static class Builder {
        private Long id;
        private Long version;
        private boolean deleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
        private String accountNumber;
        private String ownerName;
        private BigDecimal balance;
        private Bank bank;

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
        public Builder accountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }
        public Builder ownerName(String ownerName) {
            this.ownerName = ownerName;
            return this;
        }
        public Builder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }
        public Builder bank(Bank bank) {
            this.bank = bank;
            return this;
        }

        public BankAccount build() {
            return new BankAccount(this);
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
        if(this.version == null){
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
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        BankAccount bankAccount = (BankAccount) o;
        return id != null && id.equals(bankAccount.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
