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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public LocalDateTime getSendDate() {
        return sendDate;
    }

    public void setSendDate(LocalDateTime sendDate) {
        this.sendDate = sendDate;
    }

    public LocalDateTime getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(LocalDateTime receiveDate) {
        this.receiveDate = receiveDate;
    }

    public String getDeliveryNote() {
        return deliveryNote;
    }

    public void setDeliveryNote(String deliveryNote) {
        this.deliveryNote = deliveryNote;
    }

    public Courier getCourier() {
        return courier;
    }

    public void setCourier(Courier courier) {
        this.courier = courier;
    }

    // ==================== No-Arg Constructor ====================

    public Delivery() {

    }

    // ==================== All-Arguments Constructor ====================

    public Delivery(Long id,
                    Long version,
                    boolean deleted,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt,
                    LocalDateTime deletedAt,
                    String address,
                    DeliveryStatus status,
                    String trackingNumber,
                    LocalDateTime sendDate,
                    LocalDateTime receiveDate,
                    String deliveryNote,
                    Courier courier) {
        this.id = id;
        this.version = version;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.address = address;
        this.status = status;
        this.trackingNumber = trackingNumber;
        this.sendDate = sendDate;
        this.receiveDate = receiveDate;
        this.deliveryNote = deliveryNote;
        this.courier = courier;
    }

    // ==================== Builder ====================

    private Delivery(Builder builder) {
        this.id = builder.id;
        this.version = builder.version;
        this.deleted = builder.deleted;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.deletedAt = builder.deletedAt;
        this.address = builder.address;
        this.status = builder.status;
        this.trackingNumber = builder.trackingNumber;
        this.sendDate = builder.sendDate;
        this.receiveDate = builder.receiveDate;
        this.deliveryNote = builder.deliveryNote;
        this.courier = builder.courier;
    }

    private static class Builder {
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
        public Builder address(String address) {
            this.address = address;
            return this;
        }
        public Builder status(DeliveryStatus status) {
            this.status = status;
            return this;
        }
        public Builder trackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
            return this;
        }
        public Builder sendDate(LocalDateTime sendDate) {
            this.sendDate = sendDate;
            return this;
        }
        public Builder receiveDate(LocalDateTime receiveDate) {
            this.receiveDate = receiveDate;
            return this;
        }
        public Builder deliveryNote(String deliveryNote) {
            this.deliveryNote = deliveryNote;
            return this;
        }
        public Builder courier(Courier courier) {
            this.courier = courier;
            return this;
        }

        public Delivery build() {
            return new Delivery(this);
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
        if(this.status == null){
            this.status = DeliveryStatus.PREPARING;
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
        Delivery delivery = (Delivery) o;
        return id != null && id.equals(delivery.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
