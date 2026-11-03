package com.black.onlinestorese.financials.repository;

import com.black.onlinestorese.financials.entity.Payment;
import com.black.onlinestorese.financials.entity.enums.PaymentStatus;
import com.black.onlinestorese.financials.entity.enums.PaymentType;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentRepository {

    // ==================== Database Configuration ====================

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/onlinestore_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "postgres123";

    // ==================== Connection Management ====================

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found.", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // ==================== CRUD Operations ====================
    // ==================== Create/Save ====================

    public Payment save(Payment payment) {
        payment.onCreated();

        String sql = "INSERT INTO payments (version, deleted, created_at, updated_at, deleted_at, amount, status, type, order_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, payment.getVersion() != null ? payment.getVersion() : 0L);
                    preparedStatement.setBoolean(2, payment.isDeleted());
                    preparedStatement.setTimestamp(3, payment.getCreatedAt() != null ? Timestamp.valueOf(payment.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, payment.getUpdatedAt() != null ? Timestamp.valueOf(payment.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, payment.getDeletedAt() != null ? Timestamp.valueOf(payment.getDeletedAt()) : null);

                    if (payment.getAmount() != null) {
                        preparedStatement.setBigDecimal(6, payment.getAmount());
                    } else {
                        preparedStatement.setNull(6, Types.NUMERIC);
                    }

                    if (payment.getStatus() != null) {
                        preparedStatement.setString(7, payment.getStatus().name());
                    } else {
                        preparedStatement.setNull(7, Types.VARCHAR);
                    }

                    if (payment.getType() != null) {
                        preparedStatement.setString(8, payment.getType().name());
                    } else {
                        preparedStatement.setNull(8, Types.VARCHAR);
                    }

                    if (payment.getOrderId() != null) {
                        preparedStatement.setLong(9, payment.getOrderId());
                    } else {
                        preparedStatement.setNull(9, Types.BIGINT);
                    }

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating payment failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            payment.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating payment failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return payment;
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

    public Optional<Payment> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, amount, status, type, order_id " +
                "FROM payments WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToPayment(resultSet));
                        }
                        return Optional.empty();
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
            throw new RuntimeException("Database error in findById(): " + e.getMessage(), e);
        }
    }

    // ==================== Read/Find All ====================

    public List<Payment> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, amount, status, type, order_id " +
                "FROM payments WHERE deleted = false ORDER BY id ASC";

        List<Payment> payments = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            payments.add(mapRowToPayment(resultSet));
                        }
                        return payments;
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
    }

    // ==================== Update ====================

    public Payment update(Payment payment) {
        payment.onUpdated();

        String sql = "UPDATE payments SET version = ?, updated_at = ?, amount = ?, status = ?, type = ?, order_id = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, payment.getVersion() != null ? payment.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, payment.getUpdatedAt() != null ? Timestamp.valueOf(payment.getUpdatedAt()) : null);

                    if (payment.getAmount() != null) {
                        preparedStatement.setBigDecimal(3, payment.getAmount());
                    } else {
                        preparedStatement.setNull(3, Types.NUMERIC);
                    }

                    if (payment.getStatus() != null) {
                        preparedStatement.setString(4, payment.getStatus().name());
                    } else {
                        preparedStatement.setNull(4, Types.VARCHAR);
                    }

                    if (payment.getType() != null) {
                        preparedStatement.setString(5, payment.getType().name());
                    } else {
                        preparedStatement.setNull(5, Types.VARCHAR);
                    }

                    if (payment.getOrderId() != null) {
                        preparedStatement.setLong(6, payment.getOrderId());
                    } else {
                        preparedStatement.setNull(6, Types.BIGINT);
                    }

                    preparedStatement.setLong(7, payment.getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Payment update failed, record not found or marked as deleted.");
                    }

                    payment.setVersion(payment.getVersion() != null ? payment.getVersion() + 1 : 1L);
                    return payment;
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

    public boolean softDelete(Long id) {
        Payment payment = new Payment();
        payment.onDeleted();

        String sql = "UPDATE payments SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, payment.getDeletedAt() != null ? Timestamp.valueOf(payment.getDeletedAt()) : null);
                    preparedStatement.setLong(2, id);

                    int affectedRows = preparedStatement.executeUpdate();
                    return affectedRows > 0;
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

    public List<Payment> findByOrder(Long orderId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, amount, status, type, order_id " +
                "FROM payments WHERE order_id = ? AND deleted = false ORDER BY id ASC";

        List<Payment> payments = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    if (orderId != null) {
                        preparedStatement.setLong(1, orderId);
                    } else {
                        preparedStatement.setNull(1, Types.BIGINT);
                    }

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            payments.add(mapRowToPayment(resultSet));
                        }
                        return payments;
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
    }

    // ==================== Row Mappers ====================

    private Payment mapRowToPayment(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null;

        Timestamp updatedAtTimestamp = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = updatedAtTimestamp != null ? updatedAtTimestamp.toLocalDateTime() : null;

        Timestamp deletedAtTimestamp = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = deletedAtTimestamp != null ? deletedAtTimestamp.toLocalDateTime() : null;

        BigDecimal amount = resultSet.getBigDecimal("amount");

        String statusStr = resultSet.getString("status");
        PaymentStatus status = statusStr != null ? PaymentStatus.valueOf(statusStr) : null;

        String typeStr = resultSet.getString("type");
        PaymentType type = typeStr != null ? PaymentType.valueOf(typeStr) : null;

        Long orderId = resultSet.getObject("order_id", Long.class);

        return Payment.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .amount(amount)
                .status(status)
                .type(type)
                .orderId(orderId)
                .build();
    }
}
