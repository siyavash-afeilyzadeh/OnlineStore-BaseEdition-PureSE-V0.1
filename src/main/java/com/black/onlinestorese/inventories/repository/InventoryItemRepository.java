package com.black.onlinestorese.inventories.repository;

import com.black.onlinestorese.inventories.entity.InventoryItem;
import com.black.onlinestorese.inventories.entity.Inventory;
import com.black.onlinestorese.products.entity.Product;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryItemRepository {

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

    public InventoryItem save(InventoryItem entity) {
        entity.onCreated();
        String sql = "INSERT INTO inventory_items (version, deleted, created_at, updated_at, deleted_at, quantity, minimum_quantity, batch_number, expiration_date, inventory_id, product_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                    preparedStatement.setInt(7, entity.getMinimumQuantity());
                    if (entity.getBatchNumber() != null) preparedStatement.setString(8, entity.getBatchNumber());
                    else preparedStatement.setNull(8, Types.VARCHAR);
                    preparedStatement.setDate(9, entity.getExpirationDate() != null ? Date.valueOf(entity.getExpirationDate()) : null);
                    if ((entity.getInventory() != null ? entity.getInventory().getId() : null) != null) preparedStatement.setLong(10, entity.getInventory() != null ? entity.getInventory().getId() : null);
                    else preparedStatement.setNull(10, Types.BIGINT);
                    if ((entity.getProduct() != null ? entity.getProduct().getId() : null) != null) preparedStatement.setLong(11, entity.getProduct() != null ? entity.getProduct().getId() : null);
                    else preparedStatement.setNull(11, Types.BIGINT);
                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) throw new SQLException("Creating InventoryItem failed, no rows affected.");
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) entity.setId(generatedKeys.getLong(1));
                        else throw new SQLException("Creating InventoryItem failed, no ID obtained.");
                    } finally { generatedKeys.close(); }
                    return entity;
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in save(): " + e.getMessage(), e);
        }
    }

    // ==================== Read/Find By ID ====================

    public Optional<InventoryItem> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, minimum_quantity, batch_number, expiration_date, inventory_id, product_id FROM inventory_items WHERE id = ? AND deleted = false";
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        return resultSet.next() ? Optional.of(mapRowToInventoryItem(resultSet)) : Optional.empty();
                    } finally { resultSet.close(); }
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findById(): " + e.getMessage(), e);
        }
    }

    // ==================== Read/Find All ====================

    public List<InventoryItem> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, minimum_quantity, batch_number, expiration_date, inventory_id, product_id FROM inventory_items WHERE deleted = false ORDER BY id";
        List<InventoryItem> results = new ArrayList<>();
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) results.add(mapRowToInventoryItem(resultSet));
                        return results;
                    } finally { resultSet.close(); }
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findAll(): " + e.getMessage(), e);
        }
    }

    // ==================== Update ====================

    public InventoryItem update(InventoryItem entity) {
        entity.onUpdated();
        String sql = "UPDATE inventory_items SET version = ?, deleted = ?, updated_at = ?, deleted_at = ?, quantity = ?, minimum_quantity = ?, batch_number = ?, expiration_date = ?, inventory_id = ?, product_id = ? WHERE id = ? AND deleted = false";
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
                    preparedStatement.setInt(6, entity.getMinimumQuantity());
                    if (entity.getBatchNumber() != null) preparedStatement.setString(7, entity.getBatchNumber());
                    else preparedStatement.setNull(7, Types.VARCHAR);
                    preparedStatement.setDate(8, entity.getExpirationDate() != null ? Date.valueOf(entity.getExpirationDate()) : null);
                    if ((entity.getInventory() != null ? entity.getInventory().getId() : null) != null) preparedStatement.setLong(9, entity.getInventory() != null ? entity.getInventory().getId() : null);
                    else preparedStatement.setNull(9, Types.BIGINT);
                    if ((entity.getProduct() != null ? entity.getProduct().getId() : null) != null) preparedStatement.setLong(10, entity.getProduct() != null ? entity.getProduct().getId() : null);
                    else preparedStatement.setNull(10, Types.BIGINT);
                    preparedStatement.setLong(11, entity.getId());
                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) throw new SQLException("InventoryItem update failed, record not found or marked as deleted.");
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
        InventoryItem entity = new InventoryItem();
        entity.onDeleted();
        String sql = "UPDATE inventory_items SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";
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

    // ==================== Find By Inventory ====================

    public List<InventoryItem> findByInventory(Long inventoryId) {
        return queryItems("SELECT id, version, deleted, created_at, updated_at, deleted_at, quantity, minimum_quantity, batch_number, expiration_date, inventory_id, product_id FROM inventory_items WHERE inventory_id = ? AND deleted = false ORDER BY id", inventoryId, null);
    }

    // ==================== Find By Inventory And Product ====================

    public Optional<InventoryItem> findByInventoryAndProduct(Long inventoryId, Long productId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, quantity, minimum_quantity, batch_number, expiration_date, inventory_id, product_id FROM inventory_items WHERE inventory_id = ? AND product_id = ? AND deleted = false ORDER BY id";
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, inventoryId);
                    preparedStatement.setLong(2, productId);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try { return resultSet.next() ? Optional.of(mapRowToInventoryItem(resultSet)) : Optional.empty(); }
                    finally { resultSet.close(); }
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) { throw new RuntimeException("Database error in findByInventoryAndProduct(): " + e.getMessage(), e); }
    }

    // ==================== Find Low Stock ====================

    public List<InventoryItem> findLowStock() {
        return queryItems("SELECT id, version, deleted, created_at, updated_at, deleted_at, quantity, minimum_quantity, batch_number, expiration_date, inventory_id, product_id FROM inventory_items WHERE quantity < minimum_quantity AND deleted = false ORDER BY id", null, null);
    }

    // ==================== Count By Inventory ====================

    public long countByInventory(Long inventoryId) {
        String sql = "SELECT COUNT(*) FROM inventory_items WHERE inventory_id = ? AND deleted = false";
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, inventoryId);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try { resultSet.next(); return resultSet.getLong(1); }
                    finally { resultSet.close(); }
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) { throw new RuntimeException("Database error in countByInventory(): " + e.getMessage(), e); }
    }

    private List<InventoryItem> queryItems(String sql, Long inventoryId, Long unused) {
        List<InventoryItem> results = new ArrayList<>();
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    if (inventoryId != null) preparedStatement.setLong(1, inventoryId);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try { while (resultSet.next()) results.add(mapRowToInventoryItem(resultSet)); return results; }
                    finally { resultSet.close(); }
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) { throw new RuntimeException("Database error in specialized item query: " + e.getMessage(), e); }
    }

    // ==================== Row Mappers ====================

    private InventoryItem mapRowToInventoryItem(ResultSet resultSet) throws SQLException {
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
        int minimumQuantity = resultSet.getInt("minimum_quantity");
        String batchNumber = resultSet.getString("batch_number");
        Date expDate = resultSet.getDate("expiration_date");
        LocalDate expirationDate = (expDate != null) ? expDate.toLocalDate() : null;

        Long inventoryId = resultSet.getLong("inventory_id");
        Long productId = resultSet.getLong("product_id");

        Inventory inventory = new Inventory();
        inventory.setId(inventoryId);

        Product product = new Product();
        product.setId(productId);

        return InventoryItem.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .quantity(quantity)
                .minimumQuantity(minimumQuantity)
                .batchNumber(batchNumber)
                .expirationDate(expirationDate)
                .inventory(inventory)
                .product(product)
                .build();
    }

}
