package com.black.onlinestorese.financials.repository;

import com.black.onlinestorese.financials.entity.Transaction;
import com.black.onlinestorese.financials.entity.BankAccount;
import com.black.onlinestorese.financials.entity.Cashier;
import com.black.onlinestorese.financials.entity.enums.TransactionType;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepository {

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

    public Transaction save(Transaction transaction) {
        transaction.onCreated();

        String sql = "INSERT INTO transactions (version, deleted, created_at, updated_at, deleted_at, transaction_type, amount, description, bank_account_id, cashier_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, transaction.getVersion() != null ? transaction.getVersion() : 0L);
                    preparedStatement.setBoolean(2, transaction.isDeleted());
                    preparedStatement.setTimestamp(3, transaction.getCreatedAt() != null ? Timestamp.valueOf(transaction.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, transaction.getUpdatedAt() != null ? Timestamp.valueOf(transaction.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, transaction.getDeletedAt() != null ? Timestamp.valueOf(transaction.getDeletedAt()) : null);

                    if (transaction.getTransactionType() != null) {
                        preparedStatement.setString(6, transaction.getTransactionType().name());
                    } else {
                        preparedStatement.setNull(6, Types.VARCHAR);
                    }

                    if (transaction.getAmount() != null) {
                        preparedStatement.setBigDecimal(7, transaction.getAmount());
                    } else {
                        preparedStatement.setNull(7, Types.NUMERIC);
                    }

                    if (transaction.getDescription() != null) {
                        preparedStatement.setString(8, transaction.getDescription());
                    } else {
                        preparedStatement.setNull(8, Types.VARCHAR);
                    }

                    if (transaction.getBankAccount() != null && transaction.getBankAccount().getId() != null) {
                        preparedStatement.setLong(9, transaction.getBankAccount().getId());
                    } else {
                        preparedStatement.setNull(9, Types.BIGINT);
                    }

                    if (transaction.getCashier() != null && transaction.getCashier().getId() != null) {
                        preparedStatement.setLong(10, transaction.getCashier().getId());
                    } else {
                        preparedStatement.setNull(10, Types.BIGINT);
                    }

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating transaction failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            transaction.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating transaction failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return transaction;
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

    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, transaction_type, amount, description, bank_account_id, cashier_id " +
                "FROM transactions WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToTransaction(resultSet));
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

    public List<Transaction> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, transaction_type, amount, description, bank_account_id, cashier_id " +
                "FROM transactions WHERE deleted = false ORDER BY id ASC";

        List<Transaction> transactions = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            transactions.add(mapRowToTransaction(resultSet));
                        }
                        return transactions;
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

    public Transaction update(Transaction transaction) {
        transaction.onUpdated();

        String sql = "UPDATE transactions SET version = ?, updated_at = ?, transaction_type = ?, amount = ?, description = ?, bank_account_id = ?, cashier_id = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, transaction.getVersion() != null ? transaction.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, transaction.getUpdatedAt() != null ? Timestamp.valueOf(transaction.getUpdatedAt()) : null);

                    if (transaction.getTransactionType() != null) {
                        preparedStatement.setString(3, transaction.getTransactionType().name());
                    } else {
                        preparedStatement.setNull(3, Types.VARCHAR);
                    }

                    if (transaction.getAmount() != null) {
                        preparedStatement.setBigDecimal(4, transaction.getAmount());
                    } else {
                        preparedStatement.setNull(4, Types.NUMERIC);
                    }

                    if (transaction.getDescription() != null) {
                        preparedStatement.setString(5, transaction.getDescription());
                    } else {
                        preparedStatement.setNull(5, Types.VARCHAR);
                    }

                    if (transaction.getBankAccount() != null && transaction.getBankAccount().getId() != null) {
                        preparedStatement.setLong(6, transaction.getBankAccount().getId());
                    } else {
                        preparedStatement.setNull(6, Types.BIGINT);
                    }

                    if (transaction.getCashier() != null && transaction.getCashier().getId() != null) {
                        preparedStatement.setLong(7, transaction.getCashier().getId());
                    } else {
                        preparedStatement.setNull(7, Types.BIGINT);
                    }

                    preparedStatement.setLong(8, transaction.getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Transaction update failed, record not found or marked as deleted.");
                    }

                    transaction.setVersion(transaction.getVersion() != null ? transaction.getVersion() + 1 : 1L);
                    return transaction;
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
        Transaction transaction = new Transaction();
        transaction.onDeleted();

        String sql = "UPDATE transactions SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, transaction.getDeletedAt() != null ? Timestamp.valueOf(transaction.getDeletedAt()) : null);
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

    // ==================== Find By Bank Account ====================

    public List<Transaction> findByBankAccount(Long bankAccountId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, transaction_type, amount, description, bank_account_id, cashier_id " +
                "FROM transactions WHERE bank_account_id = ? AND deleted = false ORDER BY id ASC";

        List<Transaction> transactions = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    if (bankAccountId != null) {
                        preparedStatement.setLong(1, bankAccountId);
                    } else {
                        preparedStatement.setNull(1, Types.BIGINT);
                    }

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            transactions.add(mapRowToTransaction(resultSet));
                        }
                        return transactions;
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
            throw new RuntimeException("Database error in findByBankAccount(): " + e.getMessage(), e);
        }
    }

    // ==================== Row Mappers ====================

    private Transaction mapRowToTransaction(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null;

        Timestamp updatedAtTimestamp = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = updatedAtTimestamp != null ? updatedAtTimestamp.toLocalDateTime() : null;

        Timestamp deletedAtTimestamp = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = deletedAtTimestamp != null ? deletedAtTimestamp.toLocalDateTime() : null;

        String transactionTypeStr = resultSet.getString("transaction_type");
        TransactionType transactionType = transactionTypeStr != null ? TransactionType.valueOf(transactionTypeStr) : null;

        BigDecimal amount = resultSet.getBigDecimal("amount");
        String description = resultSet.getString("description");

        Long bankAccountId = resultSet.getObject("bank_account_id", Long.class);
        BankAccount bankAccount = bankAccountId != null ? BankAccount.builder().id(bankAccountId).build() : null;

        Long cashierId = resultSet.getObject("cashier_id", Long.class);
        Cashier cashier = cashierId != null ? Cashier.builder().id(cashierId).build() : null;

        return Transaction.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .transactionType(transactionType)
                .amount(amount)
                .description(description)
                .bankAccount(bankAccount)
                .cashier(cashier)
                .build();
    }
}
