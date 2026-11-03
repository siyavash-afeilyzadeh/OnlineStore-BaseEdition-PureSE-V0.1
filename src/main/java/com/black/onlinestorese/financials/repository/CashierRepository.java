package com.black.onlinestorese.financials.repository;

import com.black.onlinestorese.financials.entity.Cashier;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CashierRepository {

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

    public Cashier save(Cashier cashier) {
        cashier.onCreated();

        String sql = "INSERT INTO cashiers (version, deleted, created_at, updated_at, deleted_at, cashier_name, employee_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, cashier.getVersion() != null ? cashier.getVersion() : 0L);
                    preparedStatement.setBoolean(2, cashier.isDeleted());
                    preparedStatement.setTimestamp(3, cashier.getCreatedAt() != null ? Timestamp.valueOf(cashier.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, cashier.getUpdatedAt() != null ? Timestamp.valueOf(cashier.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, cashier.getDeletedAt() != null ? Timestamp.valueOf(cashier.getDeletedAt()) : null);
                    preparedStatement.setString(6, cashier.getCashierName());
                    preparedStatement.setString(7, cashier.getEmployeeCode());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating cashier failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            cashier.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating cashier failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return cashier;
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

    public Optional<Cashier> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, cashier_name, employee_code " +
                "FROM cashiers WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToCashier(resultSet));
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

    public List<Cashier> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, cashier_name, employee_code " +
                "FROM cashiers WHERE deleted = false ORDER BY id ASC";

        List<Cashier> cashiers = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            cashiers.add(mapRowToCashier(resultSet));
                        }
                        return cashiers;
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

    public Cashier update(Cashier cashier) {
        cashier.onUpdated();

        String sql = "UPDATE cashiers SET version = ?, updated_at = ?, cashier_name = ?, employee_code = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, cashier.getVersion() != null ? cashier.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, cashier.getUpdatedAt() != null ? Timestamp.valueOf(cashier.getUpdatedAt()) : null);
                    preparedStatement.setString(3, cashier.getCashierName());
                    preparedStatement.setString(4, cashier.getEmployeeCode());
                    preparedStatement.setLong(5, cashier.getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Cashier update failed, record not found or marked as deleted.");
                    }

                    cashier.setVersion(cashier.getVersion() != null ? cashier.getVersion() + 1 : 1L);
                    return cashier;
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
        Cashier cashier = new Cashier();
        cashier.onDeleted();

        String sql = "UPDATE cashiers SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, cashier.getDeletedAt() != null ? Timestamp.valueOf(cashier.getDeletedAt()) : null);
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

    // ==================== Find By Employee Code ====================

    public Optional<Cashier> findByEmployeeCode(String employeeCode) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, cashier_name, employee_code " +
                "FROM cashiers WHERE employee_code = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, employeeCode);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToCashier(resultSet));
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
            throw new RuntimeException("Database error in findByEmployeeCode(): " + e.getMessage(), e);
        }
    }

    // ==================== Row Mappers ====================

    private Cashier mapRowToCashier(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null;

        Timestamp updatedAtTimestamp = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = updatedAtTimestamp != null ? updatedAtTimestamp.toLocalDateTime() : null;

        Timestamp deletedAtTimestamp = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = deletedAtTimestamp != null ? deletedAtTimestamp.toLocalDateTime() : null;

        String cashierName = resultSet.getString("cashier_name");
        String employeeCode = resultSet.getString("employee_code");

        return Cashier.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .cashierName(cashierName)
                .employeeCode(employeeCode)
                .build();
    }
}
