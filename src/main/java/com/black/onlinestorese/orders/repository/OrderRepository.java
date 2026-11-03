package com.black.onlinestorese.orders.repository;

import com.black.onlinestorese.financials.entity.Payment;
import com.black.onlinestorese.orders.dto.OrderRequest;
import com.black.onlinestorese.orders.entity.Order;
import com.black.onlinestorese.orders.entity.enums.OrderStatus;
import com.black.onlinestorese.orders.entity.enums.OrderType;
import com.black.onlinestorese.orders.entity.enums.ShippingMethod;
import com.black.onlinestorese.orders.entity.enums.ShippingStatus;
import com.black.onlinestorese.users.entity.User;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepository {

    // ==================== Database Configuration ====================

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/onlinestore_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "postgres123";

    // ==================== Connection Management ====================

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found in Classpath!", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // ==================== CRUD Operations ====================
    // ==================== Create/Save ====================

    public Order save(Order order) {
        order.onCreated();

        String sql = "INSERT INTO orders (version, deleted, created_at, updated_at, deleted_at, customer_id, payment_id, total, shipping_method, shipping_status, order_type, order_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, order.getVersion() != null ? order.getVersion() : 0L);
                    preparedStatement.setBoolean(2, order.isDeleted());
                    preparedStatement.setTimestamp(3, order.getCreatedAt() != null ? Timestamp.valueOf(order.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, order.getUpdatedAt() != null ? Timestamp.valueOf(order.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, order.getDeletedAt() != null ? Timestamp.valueOf(order.getDeletedAt()) : null);
                    preparedStatement.setLong(6, order.getCustomer().getId());
                    preparedStatement.setLong(7, order.getPaymentId().getId());
                    preparedStatement.setBigDecimal(8, order.getTotal());
                    preparedStatement.setString(9, order.getShippingMethod().name());
                    preparedStatement.setString(10, order.getShippingStatus().name());
                    preparedStatement.setString(11, order.getType().name());
                    preparedStatement.setString(12, order.getStatus().name());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating order failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            order.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating order failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return order;
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in save(): " + e.getMessage(), e);
        }
    }

    // ==================== Read/Find By ID ====================

    public Optional<Order> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, customer_id, payment_id, total, shipping_method, shipping_status, order_type, order_status " +
                "FROM orders WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToOrder(resultSet));
                        }
                    } finally {
                        resultSet.close();
                    }

                    return Optional.empty();
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findById(): " + e.getMessage(), e);
        }
    }

    // ==================== Read/Find All ====================

    public List<Order> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, customer_id, payment_id, total, shipping_method, shipping_status, order_type, order_status " +
                "FROM orders WHERE deleted = false ORDER BY id ASC";

        List<Order> orders = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            orders.add(mapRowToOrder(resultSet));
                        }
                    } finally {
                        resultSet.close();
                    }
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findAll(): " + e.getMessage(), e);
        }

        return orders;
    }

    // ==================== Update ====================

    public void update(Order order) {
        order.onUpdated();

        String sql = "UPDATE orders SET version = ?, updated_at = ?, customer_id = ?, payment_id = ?, total = ?, shipping_method = ?, shipping_status = ?, order_type = ?, order_status = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, order.getVersion() != null ? order.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(order.getUpdatedAt()));
                    preparedStatement.setLong(3, order.getCustomer().getId());
                    preparedStatement.setLong(4, order.getPaymentId().getId());
                    preparedStatement.setBigDecimal(5, order.getTotal());
                    preparedStatement.setString(6, order.getShippingMethod().name());
                    preparedStatement.setString(7, order.getShippingStatus().name());
                    preparedStatement.setString(8, order.getType().name());
                    preparedStatement.setString(9, order.getStatus().name());
                    preparedStatement.setLong(10, order.getId());

                    int updated = preparedStatement.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Order update failed: record not found or marked as deleted.");
                    }
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in update(): " + e.getMessage(), e);
        }
    }

    // ==================== Delete (Soft Delete) ====================

    public void softDelete(Long id) {
        Order order = new Order();
        order.onDeleted();

        String sql = "UPDATE orders SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, Timestamp.valueOf(order.getDeletedAt()));
                    preparedStatement.setLong(2, id);

                    int affected = preparedStatement.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Soft delete failed: order not found or already deleted.");
                    }
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in softDelete(): " + e.getMessage(), e);
        }
    }

    // ==================== Create Confirm Request ====================

    public OrderRequest createConfirmRequest(Order order) {
        return new OrderRequest(
                order.getOrderItems(),
                order.getCustomer(),
                order.getShippingMethod(),
                order.getShippingStatus(),
                order.getTotal(),
                order.getType(),
                OrderStatus.CONFIRMED);
    }

    // ==================== Create Cancel Request ====================

    public OrderRequest createCancelRequest(Order order) {
        return new OrderRequest(
                order.getOrderItems(),
                order.getCustomer(),
                order.getShippingMethod(),
                order.getShippingStatus(),
                order.getTotal(),
                order.getType(),
                OrderStatus.CANCELLED);
    }

    // ==================== Create Ship Request ====================

    public OrderRequest createShipRequest(Order order) {
        return new OrderRequest(
                order.getOrderItems(),
                order.getCustomer(),
                order.getShippingMethod(),
                ShippingStatus.SHIPPED,
                order.getTotal(),
                order.getType(),
                order.getStatus());
    }

    // ==================== Create Deliver Request ====================

    public OrderRequest createDeliverRequest(Order order) {
        return new OrderRequest(
                order.getOrderItems(),
                order.getCustomer(),
                order.getShippingMethod(),
                ShippingStatus.DELIVERED,
                order.getTotal(),
                order.getType(),
                order.getStatus());
    }

    // ==================== Row Mappers ====================

    private Order mapRowToOrder(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp deletedTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedTs != null) ? deletedTs.toLocalDateTime() : null;

        Long customerId = resultSet.getLong("customer_id");
        Long paymentId = resultSet.getLong("payment_id");
        BigDecimal total = resultSet.getBigDecimal("total");

        String shippingMethodStr = resultSet.getString("shipping_method");
        ShippingMethod shippingMethod = (shippingMethodStr != null) ? ShippingMethod.valueOf(shippingMethodStr) : null;

        String shippingStatusStr = resultSet.getString("shipping_status");
        ShippingStatus shippingStatus = (shippingStatusStr != null) ? ShippingStatus.valueOf(shippingStatusStr) : null;

        String orderTypeStr = resultSet.getString("order_type");
        OrderType type = (orderTypeStr != null) ? OrderType.valueOf(orderTypeStr) : null;

        String orderStatusStr = resultSet.getString("order_status");
        OrderStatus status = (orderStatusStr != null) ? OrderStatus.valueOf(orderStatusStr) : null;

        User customer = new User();
        customer.setId(customerId);

        Payment payment = new Payment();
        payment.setId(paymentId);

        return Order.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .customer(customer)
                .paymentId(payment)
                .total(total)
                .shippingMethod(shippingMethod)
                .shippingStatus(shippingStatus)
                .type(type)
                .status(status)
                .build();
    }
}