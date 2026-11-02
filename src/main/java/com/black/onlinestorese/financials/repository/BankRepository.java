package com.black.onlinestorese.financials.repository;

import com.black.onlinestorese.financials.entity.Bank;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BankRepository {

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

    public Bank save(Bank bank) {
        bank.onCreated();

        String sql = "INSERT INTO banks (version, deleted, created_at, updated_at, deleted_at, bank_name, bank_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, bank.getVersion() != null ? bank.getVersion() : 0L);
                    preparedStatement.setBoolean(2, bank.isDeleted());
                    preparedStatement.setTimestamp(3, bank.getCreatedAt() != null ? Timestamp.valueOf(bank.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, bank.getUpdatedAt() != null ? Timestamp.valueOf(bank.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, bank.getDeletedAt() != null ? Timestamp.valueOf(bank.getDeletedAt()) : null);
                    preparedStatement.setString(6, bank.getBankName());
                    preparedStatement.setString(7, bank.getBankCode());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating bank failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            bank.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating bank failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return bank;
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

    public Optional<Bank> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, bank_name, bank_code " +
                "FROM banks WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToBank(resultSet));
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

    public List<Bank> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, bank_name, bank_code " +
                "FROM banks WHERE deleted = false ORDER BY id ASC";

        List<Bank> banks = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            banks.add(mapRowToBank(resultSet));
                        }
                        return banks;
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

    public Bank update(Bank bank) {
        bank.onUpdated();

        String sql = "UPDATE banks SET version = ?, updated_at = ?, bank_name = ?, bank_code = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, bank.getVersion() != null ? bank.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, bank.getUpdatedAt() != null ? Timestamp.valueOf(bank.getUpdatedAt()) : null);
                    preparedStatement.setString(3, bank.getBankName());
                    preparedStatement.setString(4, bank.getBankCode());
                    preparedStatement.setLong(5, bank.getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Bank update failed, record not found or marked as deleted.");
                    }

                    bank.setVersion(bank.getVersion() != null ? bank.getVersion() + 1 : 1L);
                    return bank;
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
        Bank bank = new Bank();
        bank.onDeleted();

        String sql = "UPDATE banks SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, bank.getDeletedAt() != null ? Timestamp.valueOf(bank.getDeletedAt()) : null);
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

    private Bank mapRowToBank(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null;

        Timestamp updatedAtTimestamp = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = updatedAtTimestamp != null ? updatedAtTimestamp.toLocalDateTime() : null;

        Timestamp deletedAtTimestamp = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = deletedAtTimestamp != null ? deletedAtTimestamp.toLocalDateTime() : null;

        String bankName = resultSet.getString("bank_name");
        String bankCode = resultSet.getString("bank_code");

        return Bank.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .bankName(bankName)
                .bankCode(bankCode)
                .bankAccounts(new ArrayList<>())
                .build();
    }
}
