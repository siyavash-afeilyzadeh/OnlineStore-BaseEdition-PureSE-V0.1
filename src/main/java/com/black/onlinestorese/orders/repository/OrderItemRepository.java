package com.black.onlinestorese.orders.repository;

import com.black.onlinestorese.orders.entity.Order;
import com.black.onlinestorese.orders.entity.OrderItem;
import com.black.onlinestorese.products.entity.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderItemRepository {

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

    public OrderItem save(OrderItem orderItem) {
        orderItem.onCreated();

        String sql = "INSERT INTO order_items (version, deleted, created_at, updated_at, deleted_at, order_id, product_id, quantity, unit_price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, orderItem.getVersion() != null ? orderItem.getVersion() : 0L);
                    preparedStatement.setBoolean(2, orderItem.isDeleted());
                    preparedStatement.setTimestamp(3, orderItem.getCreatedAt() != null ? Timestamp.valueOf(orderItem.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, orderItem.getUpdatedAt() != null ? Timestamp.valueOf(orderItem.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, orderItem.getDeletedAt() != null ? Timestamp.valueOf(orderItem.getDeletedAt()) : null);
                    preparedStatement.setLong(6, orderItem.getOrder().getId());
                    preparedStatement.setLong(7, orderItem.getProduct().getId());
                    preparedStatement.setInt(8, orderItem.getQuantity());
                    preparedStatement.setBigDecimal(9, orderItem.getUnitPrice());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating order item failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            orderItem.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating order item failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return orderItem;
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

    public Optional<OrderItem> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, order_id, product_id, quantity, unit_price " +
                "FROM order_items WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToOrderItem(resultSet));
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

    public List<OrderItem> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, order_id, product_id, quantity, unit_price " +
                "FROM order_items WHERE deleted = false ORDER BY id ASC";

        List<OrderItem> orderItems = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            orderItems.add(mapRowToOrderItem(resultSet));
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

        return orderItems;
    }

    // ==================== Update ====================

    public void update(OrderItem orderItem) {
        orderItem.onUpdated();

        String sql = "UPDATE order_items SET version = ?, updated_at = ?, order_id = ?, product_id = ?, quantity = ?, unit_price = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, orderItem.getVersion() != null ? orderItem.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(orderItem.getUpdatedAt()));
                    preparedStatement.setLong(3, orderItem.getOrder().getId());
                    preparedStatement.setLong(4, orderItem.getProduct().getId());
                    preparedStatement.setInt(5, orderItem.getQuantity());
                    preparedStatement.setBigDecimal(6, orderItem.getUnitPrice());
                    preparedStatement.setLong(7, orderItem.getId());

                    int updated = preparedStatement.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("OrderItem update failed: record not found or marked as deleted.");
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
        OrderItem orderItem = new OrderItem();
        orderItem.onDeleted();

        String sql = "UPDATE order_items SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, Timestamp.valueOf(orderItem.getDeletedAt()));
                    preparedStatement.setLong(2, id);

                    int affected = preparedStatement.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Soft delete failed: order item not found or already deleted.");
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

    // ==================== Find By Order ====================

    public List<OrderItem> findByOrder(Long orderId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, order_id, product_id, quantity, unit_price " +
                "FROM order_items WHERE order_id = ? AND deleted = false ORDER BY id ASC";

        List<OrderItem> orderItems = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, orderId);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            orderItems.add(mapRowToOrderItem(resultSet));
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
            throw new RuntimeException("Database error in findByOrder(): " + e.getMessage(), e);
        }

        return orderItems;
    }

    // ==================== Row Mappers ====================

    private OrderItem mapRowToOrderItem(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp deletedTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedTs != null) ? deletedTs.toLocalDateTime() : null;

        Long orderId = resultSet.getLong("order_id");
        Long productId = resultSet.getLong("product_id");
        int quantity = resultSet.getInt("quantity");
        BigDecimal unitPrice = resultSet.getBigDecimal("unit_price");

        Order order = new Order();
        order.setId(orderId);

        Product product = new Product();
        product.setId(productId);

        return OrderItem.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .order(order)
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();
    }
}