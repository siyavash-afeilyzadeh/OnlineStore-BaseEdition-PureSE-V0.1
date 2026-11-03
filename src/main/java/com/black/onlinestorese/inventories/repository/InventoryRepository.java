package com.black.onlinestorese.inventories.repository;

import com.black.onlinestorese.inventories.entity.Inventory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryRepository {

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

    public Inventory save(Inventory entity) {
        entity.onCreated();
        String sql = "INSERT INTO inventories (version, deleted, created_at, updated_at, deleted_at, name, location) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
                    if (entity.getName() != null) preparedStatement.setString(6, entity.getName());
                    else preparedStatement.setNull(6, Types.VARCHAR);
                    if (entity.getLocation() != null) preparedStatement.setString(7, entity.getLocation());
                    else preparedStatement.setNull(7, Types.VARCHAR);
                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) throw new SQLException("Creating Inventory failed, no rows affected.");
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) entity.setId(generatedKeys.getLong(1));
                        else throw new SQLException("Creating Inventory failed, no ID obtained.");
                    } finally { generatedKeys.close(); }
                    return entity;
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in save(): " + e.getMessage(), e);
        }
    }

    // ==================== Read/Find By ID ====================

    public Optional<Inventory> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, location FROM inventories WHERE id = ? AND deleted = false";
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        return resultSet.next() ? Optional.of(mapRowToInventory(resultSet)) : Optional.empty();
                    } finally { resultSet.close(); }
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findById(): " + e.getMessage(), e);
        }
    }

    // ==================== Read/Find All ====================

    public List<Inventory> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, location FROM inventories WHERE deleted = false ORDER BY id";
        List<Inventory> results = new ArrayList<>();
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) results.add(mapRowToInventory(resultSet));
                        return results;
                    } finally { resultSet.close(); }
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findAll(): " + e.getMessage(), e);
        }
    }

    // ==================== Update ====================

    public Inventory update(Inventory entity) {
        entity.onUpdated();
        String sql = "UPDATE inventories SET version = ?, deleted = ?, updated_at = ?, deleted_at = ?, name = ?, location = ? WHERE id = ? AND deleted = false";
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, entity.getVersion() != null ? entity.getVersion() + 1 : 1L);
                    preparedStatement.setBoolean(2, entity.isDeleted());
                    preparedStatement.setTimestamp(3, entity.getUpdatedAt() != null ? Timestamp.valueOf(entity.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(4, entity.getDeletedAt() != null ? Timestamp.valueOf(entity.getDeletedAt()) : null);
                    if (entity.getName() != null) preparedStatement.setString(5, entity.getName());
                    else preparedStatement.setNull(5, Types.VARCHAR);
                    if (entity.getLocation() != null) preparedStatement.setString(6, entity.getLocation());
                    else preparedStatement.setNull(6, Types.VARCHAR);
                    preparedStatement.setLong(7, entity.getId());
                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) throw new SQLException("Inventory update failed, record not found or marked as deleted.");
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
        Inventory entity = new Inventory();
        entity.onDeleted();
        String sql = "UPDATE inventories SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";
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

    // ==================== Find By Name ====================

    public Optional<Inventory> findByName(String name) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, name, location FROM inventories WHERE name = ? AND deleted = false";
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, name);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        return resultSet.next() ? Optional.of(mapRowToInventory(resultSet)) : Optional.empty();
                    } finally { resultSet.close(); }
                } finally { preparedStatement.close(); }
            } finally { connection.close(); }
        } catch (SQLException e) { throw new RuntimeException("Database error in findByName(): " + e.getMessage(), e); }
    }

    // ==================== Row Mappers ====================

    private Inventory mapRowToInventory(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null;

        Timestamp updatedAtTimestamp = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = updatedAtTimestamp != null ? updatedAtTimestamp.toLocalDateTime() : null;

        Timestamp deletedAtTimestamp = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = deletedAtTimestamp != null ? deletedAtTimestamp.toLocalDateTime() : null;

        String name = resultSet.getString("name");
        String location = resultSet.getString("location");

        return Inventory.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .name(name)
                .location(location)
                .build();
    }
}
