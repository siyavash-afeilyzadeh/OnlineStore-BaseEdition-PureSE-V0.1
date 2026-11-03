package com.black.onlinestorese.financials.repository;

import com.black.onlinestorese.financials.entity.ChequePayment;
import com.black.onlinestorese.financials.entity.Bank;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChequePaymentRepository {

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

    public ChequePayment save(ChequePayment chequePayment) {
        chequePayment.onCreated();

        String sql = "INSERT INTO cheque_payments (version, deleted, created_at, updated_at, deleted_at, cheque_number, due_date, bank_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, chequePayment.getVersion() != null ? chequePayment.getVersion() : 0L);
                    preparedStatement.setBoolean(2, chequePayment.isDeleted());
                    preparedStatement.setTimestamp(3, chequePayment.getCreatedAt() != null ? Timestamp.valueOf(chequePayment.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, chequePayment.getUpdatedAt() != null ? Timestamp.valueOf(chequePayment.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, chequePayment.getDeletedAt() != null ? Timestamp.valueOf(chequePayment.getDeletedAt()) : null);
                    preparedStatement.setString(6, chequePayment.getChequeNumber());

                    if (chequePayment.getDueDate() != null) {
                        preparedStatement.setDate(7, Date.valueOf(chequePayment.getDueDate()));
                    } else {
                        preparedStatement.setNull(7, Types.DATE);
                    }

                    if (chequePayment.getBank() != null && chequePayment.getBank().getId() != null) {
                        preparedStatement.setLong(8, chequePayment.getBank().getId());
                    } else {
                        preparedStatement.setNull(8, Types.BIGINT);
                    }

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating cheque payment failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            chequePayment.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating cheque payment failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return chequePayment;
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

    public Optional<ChequePayment> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, cheque_number, due_date, bank_id " +
                "FROM cheque_payments WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToChequePayment(resultSet));
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

    public List<ChequePayment> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, cheque_number, due_date, bank_id " +
                "FROM cheque_payments WHERE deleted = false ORDER BY id ASC";

        List<ChequePayment> chequePayments = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            chequePayments.add(mapRowToChequePayment(resultSet));
                        }
                        return chequePayments;
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

    public ChequePayment update(ChequePayment chequePayment) {
        chequePayment.onUpdated();

        String sql = "UPDATE cheque_payments SET version = ?, updated_at = ?, cheque_number = ?, due_date = ?, bank_id = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, chequePayment.getVersion() != null ? chequePayment.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, chequePayment.getUpdatedAt() != null ? Timestamp.valueOf(chequePayment.getUpdatedAt()) : null);
                    preparedStatement.setString(3, chequePayment.getChequeNumber());

                    if (chequePayment.getDueDate() != null) {
                        preparedStatement.setDate(4, Date.valueOf(chequePayment.getDueDate()));
                    } else {
                        preparedStatement.setNull(4, Types.DATE);
                    }

                    if (chequePayment.getBank() != null && chequePayment.getBank().getId() != null) {
                        preparedStatement.setLong(5, chequePayment.getBank().getId());
                    } else {
                        preparedStatement.setNull(5, Types.BIGINT);
                    }

                    preparedStatement.setLong(6, chequePayment.getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Cheque payment update failed, record not found or marked as deleted.");
                    }

                    chequePayment.setVersion(chequePayment.getVersion() != null ? chequePayment.getVersion() + 1 : 1L);
                    return chequePayment;
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
        ChequePayment chequePayment = new ChequePayment();
        chequePayment.onDeleted();

        String sql = "UPDATE cheque_payments SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, chequePayment.getDeletedAt() != null ? Timestamp.valueOf(chequePayment.getDeletedAt()) : null);
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

    private ChequePayment mapRowToChequePayment(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null;

        Timestamp updatedAtTimestamp = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = updatedAtTimestamp != null ? updatedAtTimestamp.toLocalDateTime() : null;

        Timestamp deletedAtTimestamp = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = deletedAtTimestamp != null ? deletedAtTimestamp.toLocalDateTime() : null;

        String chequeNumber = resultSet.getString("cheque_number");

        Date dueDateSql = resultSet.getDate("due_date");
        LocalDate dueDate = dueDateSql != null ? dueDateSql.toLocalDate() : null;

        Long bankId = resultSet.getObject("bank_id", Long.class);
        Bank bank = bankId != null ? Bank.builder().id(bankId).build() : null;

        return ChequePayment.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .chequeNumber(chequeNumber)
                .dueDate(dueDate)
                .bank(bank)
                .build();
    }
}
