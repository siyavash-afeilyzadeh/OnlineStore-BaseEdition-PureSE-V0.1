package com.black.onlinestorese.inventories.repository;

import com.black.onlinestorese.inventories.entity.InventoryTransaction;
import com.black.onlinestorese.inventories.entity.InventoryItem;
import com.black.onlinestorese.inventories.entity.enums.InventoryReferenceType;
import com.black.onlinestorese.inventories.entity.enums.InventoryTransactionType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryTransactionRepository {

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

    public InventoryTransaction save(InventoryTransaction entity) {
        entity.onCreated();
        String sql = "INSERT INTO inventory_transactions (version, deleted, created_at, updated_at, deleted_at, quantity, inventory_transaction_type, reference_type, reference_id, reason, item_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, entity.getVersion() != null ? entity.getVersion() : 0L);
                    preparedStatement.setBoolean(2, entity.isDeleted());
                    preparedStatement.setTimestamp(3, entity.getCreatedAt() != null ? Timestamp.valueOf(entity.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, entity.getUpdatedAt() != null ? Timestamp.valueOf(entity.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, entity.getDeletedAt() != null ? Timestamp.valueOf(entity.getDeletedAt()) : null);
                    preparedStatement.setInt(6, entity.getQuantity());
                    if ((entity.getTransactionType() != null ? entity.getTransactionType().name() : null) != null) preparedStatement.setString(7, entity.getTransactionType() != null ? entity.getTransactionType().name() : null);
                    else preparedStatement.setNull(7, Types.VARCHAR);
                    if ((entity.getReferenceType() != null ? entity.getReferenceType().name() : null) != null) preparedStatement.setString(8, entity.getReferenceType() != null ? entity.getReferenceType().name() : null);
                    else preparedStatement.setNull(8, Types.VARCHAR);
                    if (entity.getReferenceId() != null) preparedStatement.setLong(9, entity.getReferenceId());
                    else preparedStatement.setNull(9, Types.BIGINT);
                    if (entity.getReason() != null) preparedStatement.setString(10, entity.getReason());
                    else preparedStatement.setNull(10, Types.VARCHAR);
                    if ((entity.getInventoryItem() != null ? entity.getInventoryItem().getId() : null) != null) preparedStatement.setLong(11, entity.getInventoryItem() != null ? entity.getInventoryItem().getId() : null);
                    else preparedStatement.setNull(11, Types.BIGINT);
                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) throw new SQLException("Creating InventoryTransaction failed, no rows affected.");
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) entity.setId(generatedKeys.getLong(1));
                        else throw new SQLException("Creating InventoryTransaction failed, no ID obtained.");
                    } finally { generatedKeys.close(); }
                    return entity;
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in save(): " + e.getMessage(), e);
        }
    }

    // ==================== Read/Find By ID ====================

    public Optional<InventoryTransaction> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, inventory_transaction_type, reference_type, reference_id, reason, item_id FROM inventory_transactions WHERE id = ? AND deleted = false";
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        return resultSet.next() ? Optional.of(mapRowToInventoryTransaction(resultSet)) : Optional.empty();
                    } finally { resultSet.close(); }
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findById(): " + e.getMessage(), e);
        }
    }

    // ==================== Read/Find All ====================

    public List<InventoryTransaction> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, inventory_transaction_type, reference_type, reference_id, reason, item_id FROM inventory_transactions WHERE deleted = false ORDER BY id";
        List<InventoryTransaction> results = new ArrayList<>();
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) results.add(mapRowToInventoryTransaction(resultSet));
                        return results;
                    } finally { resultSet.close(); }
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findAll(): " + e.getMessage(), e);
        }
    }

    // ==================== Update ====================

    public InventoryTransaction update(InventoryTransaction entity) {
        entity.onUpdated();
        String sql = "UPDATE inventory_transactions SET version = ?, deleted = ?, updated_at = ?, deleted_at = ?, quantity = ?, inventory_transaction_type = ?, reference_type = ?, reference_id = ?, reason = ?, item_id = ? WHERE id = ? AND deleted = false";
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, entity.getVersion() != null ? entity.getVersion() + 1 : 1L);
                    preparedStatement.setBoolean(2, entity.isDeleted());
                    preparedStatement.setTimestamp(3, entity.getUpdatedAt() != null ? Timestamp.valueOf(entity.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(4, entity.getDeletedAt() != null ? Timestamp.valueOf(entity.getDeletedAt()) : null);
                    preparedStatement.setInt(5, entity.getQuantity());
                    if ((entity.getTransactionType() != null ? entity.getTransactionType().name() : null) != null) preparedStatement.setString(6, entity.getTransactionType() != null ? entity.getTransactionType().name() : null);
                    else preparedStatement.setNull(6, Types.VARCHAR);
                    if ((entity.getReferenceType() != null ? entity.getReferenceType().name() : null) != null) preparedStatement.setString(7, entity.getReferenceType() != null ? entity.getReferenceType().name() : null);
                    else preparedStatement.setNull(7, Types.VARCHAR);
                    if (entity.getReferenceId() != null) preparedStatement.setLong(8, entity.getReferenceId());
                    else preparedStatement.setNull(8, Types.BIGINT);
                    if (entity.getReason() != null) preparedStatement.setString(9, entity.getReason());
                    else preparedStatement.setNull(9, Types.VARCHAR);
                    if ((entity.getInventoryItem() != null ? entity.getInventoryItem().getId() : null) != null) preparedStatement.setLong(10, entity.getInventoryItem() != null ? entity.getInventoryItem().getId() : null);
                    else preparedStatement.setNull(10, Types.BIGINT);
                    preparedStatement.setLong(11, entity.getId());
                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) throw new SQLException("InventoryTransaction update failed, record not found or marked as deleted.");
                    entity.setVersion(entity.getVersion() != null ? entity.getVersion() + 1 : 1L);
                    return entity;
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in update(): " + e.getMessage(), e);
        }
    }

    // ==================== Delete (Soft Delete) ====================

    public boolean softDelete(Long id) {
        InventoryTransaction entity = new InventoryTransaction();
        entity.onDeleted();
        String sql = "UPDATE inventory_transactions SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, entity.getDeletedAt() != null ? Timestamp.valueOf(entity.getDeletedAt()) : null);
                    preparedStatement.setLong(2, id);
                    return preparedStatement.executeUpdate() > 0;
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in softDelete(): " + e.getMessage(), e);
        }
    }

    // ==================== (Specialized Methods) ====================

    // ==================== Find By Item ====================

    public List<InventoryTransaction> findByItem(Long itemId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, quantity, inventory_transaction_type, reference_type, reference_id, reason, item_id FROM inventory_transactions WHERE item_id = ? AND deleted = false ORDER BY id";
        List<InventoryTransaction> results = new ArrayList<>();
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, itemId);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try { while (resultSet.next()) results.add(mapRowToInventoryTransaction(resultSet)); return results; }
                    finally { resultSet.close(); }
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) { throw new RuntimeException("Database error in findByItem(): " + e.getMessage(), e); }
    }

    // ==================== Row Mappers ====================

    private InventoryTransaction mapRowToInventoryTransaction(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp deletedTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedTs != null) ? deletedTs.toLocalDateTime() : null;

        int quantity = resultSet.getInt("quantity");
        String transactionTypeValue = resultSet.getString("inventory_transaction_type");
        String referenceTypeValue = resultSet.getString("reference_type");
        Long referenceId = resultSet.getObject("reference_id", Long.class);
        String reason = resultSet.getString("reason");
        Long itemId = resultSet.getLong("item_id");

        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setId(itemId);

        InventoryTransactionType transactionType = (transactionTypeValue != null)
                ? InventoryTransactionType.valueOf(transactionTypeValue)
                : null;

        InventoryReferenceType referenceType = (referenceTypeValue != null)
                ? InventoryReferenceType.valueOf(referenceTypeValue)
                : null;

        return InventoryTransaction.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .quantity(quantity)
                .transactionType(transactionType)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .reason(reason)
                .inventoryItem(inventoryItem)
                .build();
    }

}
