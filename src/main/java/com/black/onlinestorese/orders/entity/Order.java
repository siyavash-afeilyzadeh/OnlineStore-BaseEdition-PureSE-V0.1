package com.black.onlinestorese.orders.entity;

import com.black.onlinestorese.financials.entity.Payment;
import com.black.onlinestorese.orders.entity.enums.OrderStatus;
import com.black.onlinestorese.orders.entity.enums.OrderType;
import com.black.onlinestorese.orders.entity.enums.ShippingMethod;
import com.black.onlinestorese.orders.entity.enums.ShippingStatus;
import com.black.onlinestorese.users.entity.User;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private List<OrderItem> orderItems;
    private User customer;
    private ShippingMethod shippingMethod;
    private ShippingStatus shippingStatus;
    private Payment paymentId;
    private BigDecimal total;
    private OrderType type;
    private OrderStatus status;

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

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(ShippingMethod shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public ShippingStatus getShippingStatus() {
        return shippingStatus;
    }

    public void setShippingStatus(ShippingStatus shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public Payment getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Payment paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public OrderType getType() {
        return type;
    }

    public void setOrderType(OrderType type) {
        this.type = type;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setOrderStatus(OrderStatus status) {
        this.status = status;
    }

    // ==================== No-Arg Constructor ====================

    public Order() {
    }

    // ==================== All-Arguments Constructor ====================

    public Order(Long id,
                 Long version,
                 boolean deleted,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt,
                 LocalDateTime deletedAt,
                 List<OrderItem> orderItems,
                 User customer,
                 ShippingMethod shippingMethod,
                 ShippingStatus shippingStatus,
                 Payment paymentId,
                 BigDecimal total,
                 OrderType type,
                 OrderStatus status) {
        this.id = id;
        this.version = version;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.orderItems = orderItems;
        this.customer = customer;
        this.shippingMethod = shippingMethod;
        this.shippingStatus = shippingStatus;
        this.paymentId = paymentId;
        this.total = total;
        this.type = type;
        this.status = status;
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
            this.status = OrderStatus.NEW;
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
        Order order = (Order) o;
        return id != null && id.equals(order.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

}
