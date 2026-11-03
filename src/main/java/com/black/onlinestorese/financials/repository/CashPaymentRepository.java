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
        String sql = "INSERT INTO cash_payments (cashier_id, version, deleted, created_at, updated_at, deleted_at) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    if (cashPayment.getCashier() != null && cashPayment.getCashier().getId() != null) {
                        preparedStatement.setLong(1, cashPayment.getCashier().getId());
                    } else {
                        preparedStatement.setNull(1, Types.BIGINT);
                    }
                    preparedStatement.setLong(2, cashPayment.getVersion() != null ? cashPayment.getVersion() : 0L);
                    preparedStatement.setBoolean(3, cashPayment.isDeleted());
                    preparedStatement.setTimestamp(4, cashPayment.getCreatedAt() != null ? Timestamp.valueOf(cashPayment.getCreatedAt()) : Timestamp.valueOf(LocalDateTime.now()));

                    if (cashPayment.getUpdatedAt() != null) {
                        preparedStatement.setTimestamp(5, Timestamp.valueOf(cashPayment.getUpdatedAt()));
                    } else {
                        preparedStatement.setNull(5, Types.TIMESTAMP);
                    }

                    if (cashPayment.getDeletedAt() != null) {
                        preparedStatement.setTimestamp(6, Timestamp.valueOf(cashPayment.getDeletedAt()));
                    } else {
                        preparedStatement.setNull(6, Types.TIMESTAMP);
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
        String sql = "SELECT id, cashier_id, version, deleted, created_at, updated_at, deleted_at FROM cash_payments WHERE id = ? AND deleted = false";

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
        String sql = "SELECT id, cashier_id, version, deleted, created_at, updated_at, deleted_at FROM cash_payments WHERE deleted = false ORDER BY id ASC";

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
        long nextVersion = (cashPayment.getVersion() != null ? cashPayment.getVersion() : 0L) + 1;

        String sql = "UPDATE cash_payments SET cashier_id = ?, version = ?, updated_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    if (cashPayment.getCashier() != null && cashPayment.getCashier().getId() != null) {
                        preparedStatement.setLong(1, cashPayment.getCashier().getId());
                    } else {
                        preparedStatement.setNull(1, Types.BIGINT);
                    }
                    preparedStatement.setLong(2, nextVersion);
                    preparedStatement.setTimestamp(3, cashPayment.getUpdatedAt() != null ? Timestamp.valueOf(cashPayment.getUpdatedAt()) : Timestamp.valueOf(LocalDateTime.now()));
                    preparedStatement.setLong(4, cashPayment.getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Cash payment update failed, record not found or already deleted.");
                    }

                    cashPayment.setVersion(nextVersion);
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
        CashPayment temp = new CashPayment();
        temp.onDeleted();

        String sql = "UPDATE cash_payments SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, temp.getDeletedAt() != null ? Timestamp.valueOf(temp.getDeletedAt()) : Timestamp.valueOf(LocalDateTime.now()));
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
        Long cashierId = resultSet.getLong("cashier_id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : null;

        Timestamp updatedAtTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = updatedAtTs != null ? updatedAtTs.toLocalDateTime() : null;

        Timestamp deletedAtTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = deletedAtTs != null ? deletedAtTs.toLocalDateTime() : null;

        Cashier cashier = null;
        if (cashierId != 0 && !resultSet.wasNull()) {
            cashier = new Cashier();
            cashier.setId(cashierId);
        }

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
