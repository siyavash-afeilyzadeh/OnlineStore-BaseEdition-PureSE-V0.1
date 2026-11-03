package com.black.onlinestorese.financials.repository;

import com.black.onlinestorese.financials.entity.CashPayment;
import com.black.onlinestorese.financials.entity.Cashier;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CashPaymentRepository {

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

    public CashPayment save(CashPayment cashPayment) {
        cashPayment.onCreated();

        String sql = "INSERT INTO cash_payments (version, deleted, created_at, updated_at, deleted_at, cashier_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, cashPayment.getVersion() != null ? cashPayment.getVersion() : 0L);
                    preparedStatement.setBoolean(2, cashPayment.isDeleted());
                    preparedStatement.setTimestamp(3, cashPayment.getCreatedAt() != null ? Timestamp.valueOf(cashPayment.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, cashPayment.getUpdatedAt() != null ? Timestamp.valueOf(cashPayment.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, cashPayment.getDeletedAt() != null ? Timestamp.valueOf(cashPayment.getDeletedAt()) : null);

                    if (cashPayment.getCashier() != null && cashPayment.getCashier().getId() != null) {
                        preparedStatement.setLong(6, cashPayment.getCashier().getId());
                    } else {
                        preparedStatement.setNull(6, Types.BIGINT);
                    }

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating cash payment failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            cashPayment.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating cash payment failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return cashPayment;
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

    public Optional<CashPayment> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, cashier_id " +
                "FROM cash_payments WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToCashPayment(resultSet));
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

    public List<CashPayment> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, cashier_id " +
                "FROM cash_payments WHERE deleted = false ORDER BY id ASC";

        List<CashPayment> cashPayments = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            cashPayments.add(mapRowToCashPayment(resultSet));
                        }
                        return cashPayments;
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

    public CashPayment update(CashPayment cashPayment) {
        cashPayment.onUpdated();

        String sql = "UPDATE cash_payments SET version = ?, updated_at = ?, cashier_id = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, cashPayment.getVersion() != null ? cashPayment.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, cashPayment.getUpdatedAt() != null ? Timestamp.valueOf(cashPayment.getUpdatedAt()) : null);

                    if (cashPayment.getCashier() != null && cashPayment.getCashier().getId() != null) {
                        preparedStatement.setLong(3, cashPayment.getCashier().getId());
                    } else {
                        preparedStatement.setNull(3, Types.BIGINT);
                    }

                    preparedStatement.setLong(4, cashPayment.getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Cash payment update failed, record not found or marked as deleted.");
                    }

                    cashPayment.setVersion(cashPayment.getVersion() != null ? cashPayment.getVersion() + 1 : 1L);
                    return cashPayment;
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
        CashPayment cashPayment = new CashPayment();
        cashPayment.onDeleted();

        String sql = "UPDATE cash_payments SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, cashPayment.getDeletedAt() != null ? Timestamp.valueOf(cashPayment.getDeletedAt()) : null);
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

    // ==================== Row Mappers ====================

    private CashPayment mapRowToCashPayment(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null;

        Timestamp updatedAtTimestamp = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = updatedAtTimestamp != null ? updatedAtTimestamp.toLocalDateTime() : null;

        Timestamp deletedAtTimestamp = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = deletedAtTimestamp != null ? deletedAtTimestamp.toLocalDateTime() : null;

        Long cashierId = resultSet.getObject("cashier_id", Long.class);
        Cashier cashier = cashierId != null ? Cashier.builder().id(cashierId).build() : null;

        return CashPayment.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .cashier(cashier)
                .build();
    }
}