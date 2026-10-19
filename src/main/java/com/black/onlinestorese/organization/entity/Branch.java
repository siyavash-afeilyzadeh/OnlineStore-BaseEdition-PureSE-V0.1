package com.black.onlinestorese.organization.entity;

import com.black.onlinestorese.organization.entity.enums.BranchStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Branch implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Organization organization;
    private String name;
    private String branchCode;
    private String phoneNumber;
    private String email;
    private String website;
    private LocalDate openingDate;
    private BranchStatus status;
    private String address;

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

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(LocalDate openingDate) {
        this.openingDate = openingDate;
    }

    public BranchStatus getStatus() {
        return status;
    }

    public void setStatus(BranchStatus status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // ==================== No-Arg Constructor ====================

    public Branch() {

    }

    // ==================== All-Arguments Constructor ====================

    public Branch(Long id,
                  Long version,
                  boolean deleted,
                  LocalDateTime createdAt,
                  LocalDateTime updatedAt,
                  LocalDateTime deletedAt,
                  Organization organization,
                  String name,
                  String branchCode,
                  String phoneNumber,
                  String email,
                  String website,
                  LocalDate openingDate,
                  BranchStatus status,
                  String address) {
        this.id = id;
        this.version = version;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.organization = organization;
        this.name = name;
        this.branchCode = branchCode;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.website = website;
        this.openingDate = openingDate;
        this.status = status;
        this.address = address;
    }

    // ==================== Builder ====================

    private Branch(Builder builder) {
        this.id = builder.id;
        this.version = builder.version;
        this.deleted = builder.deleted;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.deletedAt = builder.deletedAt;
        this.organization = builder.organization;
        this.name = builder.name;
        this.branchCode = builder.branchCode;
        this.phoneNumber = builder.phoneNumber;
        this.email = builder.email;
        this.website = builder.website;
        this.openingDate = builder.openingDate;
        this.status = builder.status;
        this.address = builder.address;
    }

    private static class Builder {
        private Long id;
        private Long version;
        private boolean deleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
        private Organization organization;
        private String name;
        private String branchCode;
        private String phoneNumber;
        private String email;
        private String website;
        private LocalDate openingDate;
        private BranchStatus status;
        private String address;


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

        public Builder organization(Organization organization) {
            this.organization = organization;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder branchCode(String branchCode) {
            this.branchCode = branchCode;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder openingDate(LocalDate openingDate) {
            this.openingDate = openingDate;
            return this;
        }

        public Builder status(BranchStatus status) {
            this.status = status;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Branch build() {
            return new Branch(this);
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
        if (this.status == null) {
            this.status = BranchStatus.ACTIVE;
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
        Branch branch = (Branch) o;
        return id != null && id.equals(branch.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
