package com.black.onlinestorese.financials.repository;

import com.black.onlinestorese.financials.entity.Bank;
import com.black.onlinestorese.financials.entity.BankAccount;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BankAccountRepository {

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

    public BankAccount save(BankAccount bankAccount) {
        bankAccount.onCreated();

        String sql = "INSERT INTO bank_accounts (version, deleted, created_at, updated_at, deleted_at, account_number, owner_name, balance, bank_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, bankAccount.getVersion() != null ? bankAccount.getVersion() : 0L);
                    preparedStatement.setBoolean(2, bankAccount.isDeleted());
                    preparedStatement.setTimestamp(3, bankAccount.getCreatedAt() != null ? Timestamp.valueOf(bankAccount.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, bankAccount.getUpdatedAt() != null ? Timestamp.valueOf(bankAccount.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, bankAccount.getDeletedAt() != null ? Timestamp.valueOf(bankAccount.getDeletedAt()) : null);
                    preparedStatement.setString(6, bankAccount.getAccountNumber());
                    preparedStatement.setString(7, bankAccount.getOwnerName());
                    preparedStatement.setBigDecimal(8, bankAccount.getBalance() != null ? bankAccount.getBalance() : BigDecimal.ZERO);

                    if (bankAccount.getBank() != null && bankAccount.getBank().getId() != null) {
                        preparedStatement.setLong(9, bankAccount.getBank().getId());
                    } else {
                        preparedStatement.setNull(9, Types.BIGINT);
                    }

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating bank account failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            bankAccount.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating bank account failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return bankAccount;
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

    public Optional<BankAccount> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, account_number, owner_name, balance, bank_id " +
                "FROM bank_accounts WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToBankAccount(resultSet));
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

    public List<BankAccount> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, account_number, owner_name, balance, bank_id " +
                "FROM bank_accounts WHERE deleted = false ORDER BY id ASC";

        List<BankAccount> bankAccounts = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            bankAccounts.add(mapRowToBankAccount(resultSet));
                        }
                        return bankAccounts;
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

    public BankAccount update(BankAccount bankAccount) {
        bankAccount.onUpdated();

        String sql = "UPDATE bank_accounts SET version = ?, updated_at = ?, account_number = ?, owner_name = ?, balance = ?, bank_id = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, bankAccount.getVersion() != null ? bankAccount.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, bankAccount.getUpdatedAt() != null ? Timestamp.valueOf(bankAccount.getUpdatedAt()) : null);
                    preparedStatement.setString(3, bankAccount.getAccountNumber());
                    preparedStatement.setString(4, bankAccount.getOwnerName());
                    preparedStatement.setBigDecimal(5, bankAccount.getBalance() != null ? bankAccount.getBalance() : BigDecimal.ZERO);

                    if (bankAccount.getBank() != null && bankAccount.getBank().getId() != null) {
                        preparedStatement.setLong(6, bankAccount.getBank().getId());
                    } else {
                        preparedStatement.setNull(6, Types.BIGINT);
                    }

                    preparedStatement.setLong(7, bankAccount.getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Bank account update failed, record not found or marked as deleted.");
                    }

                    bankAccount.setVersion(bankAccount.getVersion() != null ? bankAccount.getVersion() + 1 : 1L);
                    return bankAccount;
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
        BankAccount bankAccount = new BankAccount();
        bankAccount.onDeleted();

        String sql = "UPDATE bank_accounts SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, bankAccount.getDeletedAt() != null ? Timestamp.valueOf(bankAccount.getDeletedAt()) : null);
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

    // ==================== Find By Account Number ====================

    public Optional<BankAccount> findByAccountNumber(String accountNumber) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, account_number, owner_name, balance, bank_id " +
                "FROM bank_accounts WHERE account_number = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, accountNumber);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToBankAccount(resultSet));
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
            throw new RuntimeException("Database error in findByAccountNumber(): " + e.getMessage(), e);
        }
    }

    // ==================== Find By Bank ====================

    public List<BankAccount> findByBank(Long bankId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, account_number, owner_name, balance, bank_id " +
                "FROM bank_accounts WHERE bank_id = ? AND deleted = false ORDER BY id ASC";

        List<BankAccount> bankAccounts = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, bankId);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            bankAccounts.add(mapRowToBankAccount(resultSet));
                        }
                        return bankAccounts;
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
            throw new RuntimeException("Database error in findByBank(): " + e.getMessage(), e);
        }
    }

    // ==================== Count By Bank ====================

    public long countByBank(Long bankId) {
        String sql = "SELECT COUNT(*) FROM bank_accounts WHERE bank_id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, bankId);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return resultSet.getLong(1);
                        }
                        return 0L;
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
            throw new RuntimeException("Database error in countByBank(): " + e.getMessage(), e);
        }
    }

    // ==================== Find By Id For Update ====================

    public Optional<BankAccount> findByIdForUpdate(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, account_number, owner_name, balance, bank_id " +
                "FROM bank_accounts WHERE id = ? AND deleted = false FOR UPDATE";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToBankAccount(resultSet));
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
            throw new RuntimeException("Database error in findByIdForUpdate(): " + e.getMessage(), e);
        }
    }

    // ==================== Row Mappers ====================

    private BankAccount mapRowToBankAccount(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null;

        Timestamp updatedAtTimestamp = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = updatedAtTimestamp != null ? updatedAtTimestamp.toLocalDateTime() : null;

        Timestamp deletedAtTimestamp = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = deletedAtTimestamp != null ? deletedAtTimestamp.toLocalDateTime() : null;

        String accountNumber = resultSet.getString("account_number");
        String ownerName = resultSet.getString("owner_name");
        BigDecimal balance = resultSet.getBigDecimal("balance");

        Long bankId = resultSet.getLong("bank_id");
        Bank bank = null;
        if (!resultSet.wasNull() && bankId != 0) {
            bank = new Bank();
            bank.setId(bankId);
        }

        return BankAccount.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .accountNumber(accountNumber)
                .ownerName(ownerName)
                .balance(balance)
                .bank(bank)
                .build();
    }
}
