package com.black.onlinestorese.users.repository;

import com.black.onlinestorese.users.entity.Permission;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PermissionRepository {

    // ==================== Database Configuration ====================

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/onlinestore_db";
    private static final String DB_User = "postgres";
    private static final String DB_Pass = "postgres123";

    // ==================== Connection Management ====================

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found in Classpath!", e);
        }
        return DriverManager.getConnection(DB_URL, DB_User, DB_Pass);
    }

    // ==================== CRUD Operations ====================
    // ==================== Create/Save ====================

    public Permission save(Permission permission) {
        permission.onCreated();

        String sql = "INSERT INTO permissions (version, deleted, created_at, updated_at, deleted_at, name) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, permission.getVersion() != null ? permission.getVersion() : 0L);
                    preparedStatement.setBoolean(2, permission.isDeleted());
                    preparedStatement.setTimestamp(3, permission.getCreatedAt() != null ? Timestamp.valueOf(permission.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, permission.getUpdatedAt() != null ? Timestamp.valueOf(permission.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, permission.getDeletedAt() != null ? Timestamp.valueOf(permission.getDeletedAt()) : null);
                    preparedStatement.setString(6, permission.getName());

                    int affectedRow = preparedStatement.executeUpdate();
                    if (affectedRow == 0) {
                        throw new SQLException("Creating permission failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            permission.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating permission failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return permission;
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

    public Optional<Permission> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, name " +
                "FROM permissions WHERE id =? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToPermission(resultSet));
                        }
                    } finally {
                        resultSet.close();
                    }

                    return Optional.empty();
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

    // ==================== Read/Find All ====================

    public List<Permission> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, name " +
                "FROM permissions WHERE deleted = false ORDER BY id ASC";

        List<Permission> permissions = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            permissions.add(mapRowToPermission(resultSet));
                        }
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

        return permissions;
    }

    // ==================== Update ====================

    public void update(Permission permission) {
        permission.onUpdated();

        String sql = "UPDATE permissions SET version = ?, updated_at = ?, name = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, permission.getVersion() != null ? permission.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(permission.getUpdatedAt()));
                    preparedStatement.setString(3, permission.getName());
                    preparedStatement.setLong(4, permission.getId());

                    int updated = preparedStatement.executeUpdate();
                    if(updated == 0) {
                        throw new SQLException("Permission update failed: record not found or marked as deleted.");
                    }
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

    public void softDelete(Long id) {
        Permission permission = new Permission();
        permission.onDeleted();

        String sql = "UPDATE permissions SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, Timestamp.valueOf(permission.getDeletedAt()));
                    preparedStatement.setLong(2, id);

                    int affected = preparedStatement.executeUpdate();
                    if(affected == 0) {
                        throw new SQLException("Soft delete failed: permission not found or already deleted.");
                    }
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

    private Permission mapRowToPermission(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp deletedTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedTs != null) ? deletedTs.toLocalDateTime() : null;

        String name = resultSet.getString("name");

        return Permission.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .name(name)
                .build();
    }
}
