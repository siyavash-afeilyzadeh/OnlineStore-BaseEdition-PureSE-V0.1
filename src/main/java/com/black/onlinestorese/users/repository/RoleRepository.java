package com.black.onlinestorese.users.repository;

import com.black.onlinestorese.users.entity.Permission;
import com.black.onlinestorese.users.entity.Role;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class RoleRepository {

    // ==================== Database Configuration ====================

    private static final String DB_URL = "jdbc:postgres://localhost:5432/onlinestore_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "postgres123";

    // ==================== Connection Management ====================

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found in Classpath!", e);
        }

        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // ==================== CRUD Operations ====================
    // ==================== Create/Save ====================

    public Role save(Role role) {
        role.onCreated();

        String sql = "INSERT INTO roles (version, deleted, created_at, updated_at, deleted_at, name) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, role.getVersion() != null ? role.getVersion() : null);
                    preparedStatement.setBoolean(2, role.isDeleted());
                    preparedStatement.setTimestamp(3, role.getCreatedAt() != null ? Timestamp.valueOf(role.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, role.getUpdatedAt() != null ? Timestamp.valueOf(role.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, role.getDeletedAt() != null ? Timestamp.valueOf(role.getDeletedAt()) : null);
                    preparedStatement.setString(6, role.getName());

                    int affectedRow = preparedStatement.executeUpdate();
                    if (affectedRow == 0) {
                        throw new SQLException("Creating role failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            role.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating role failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return role;
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

    public Optional<Role> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, name " +
                "FROM roles WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToRole(resultSet));
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
            throw new RuntimeException("Database error in findById(): " + e.getMessage(), e);
        }
    }

    // ==================== Read/Find All ====================

    public List<Role> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, name " +
                "FROM roles WHERE deleted = false ORDER BY id ASC";

        List<Role> roles = new ArrayList<>();
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            roles.add(mapRowToRole(resultSet));
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

        return roles;
    }

    // ==================== Update ====================

    public void update(Role role) {
        role.onUpdated();

        String sql = "UPDATE roles SET version = ?, updated_at = ?, name = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, role.getVersion() != null ? role.getVersion() : null);
                    preparedStatement.setTimestamp(2, role.getUpdatedAt() != null ? Timestamp.valueOf(role.getUpdatedAt()) : null);
                    preparedStatement.setString(3, role.getName());
                    preparedStatement.setLong(4, role.getId());

                    int updated = preparedStatement.executeUpdate();
                    if(updated == 0) {
                        throw new SQLException("Role update failed: record not found or marked as deleted.");
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
        Role role = new Role();
        role.onDeleted();

        String sql = "UPDATE roles SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, role.getDeletedAt() != null ? Timestamp.valueOf(role.getDeletedAt()) : null);
                    preparedStatement.setLong(2, id);

                    int affected = preparedStatement.executeUpdate();
                    if(affected == 0) {
                        throw new SQLException("Soft delete failed: role not found or already deleted.");
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

    // ==================== Find By Name ====================

    public Optional<Role> findByName (String name) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, name " +
                "FROM roles WHERE name = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, name);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToRole(resultSet));
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
            throw new RuntimeException("Database error in findById(): " + e.getMessage(), e);
        }
    }

    // ==================== Helper Methods for Permissions Association ====================
    private Set<Permission> findPermissionsByRoleId(Connection connection, Long roleId) throws SQLException {
        Set<Permission> permissions = new HashSet<>();
        String sql = "SELECT p.id, p.version, p.deleted, p.created_at, p.updated_at, p.deleted_at, p.name, p.description " +
                "FROM permissions p " +
                "JOIN role_permissions rp ON rp.permission_id = p.id " +
                "WHERE rp.role_id = ? AND p.deleted = false";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        try {
            preparedStatement.setLong(1, roleId);
            ResultSet resultSet = preparedStatement.executeQuery();
            try {
                while (resultSet.next()) {
                    Timestamp createdAtTs = resultSet.getTimestamp("created_at");
                    Timestamp updatedAtTs = resultSet.getTimestamp("updated_at");
                    Timestamp deletedAtTs = resultSet.getTimestamp("deleted_at");

                    Permission permission = Permission.builder()
                            .id(resultSet.getLong("id"))
                            .version(resultSet.getLong("version"))
                            .deleted(resultSet.getBoolean("deleted"))
                            .createdAt(createdAtTs != null ? createdAtTs.toLocalDateTime() : null)
                            .updatedAt(updatedAtTs != null ? updatedAtTs.toLocalDateTime() : null)
                            .deletedAt(deletedAtTs != null ? deletedAtTs.toLocalDateTime() : null)
                            .name(resultSet.getString("name"))
                            // Assuming permission has description or adjust based on Permission entity
                            .build();
                    permissions.add(permission);
                }
            } finally {
                resultSet.close();
            }
        } finally {
            preparedStatement.close();
        }

        return permissions;
    }

    private void saveRolePermissions(Connection connection, Long roleId, Set<Permission> permissions) throws SQLException {
        String sql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        try {
            for (Permission permission : permissions) {
                if (permission.getId() != null) {
                    preparedStatement.setLong(1, roleId);
                    preparedStatement.setLong(2, permission.getId());
                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();
        } finally {
            preparedStatement.close();
        }
    }

    private void deleteRolePermissions(Connection connection, Long roleId) throws SQLException {
        String sql = "DELETE FROM role_permissions WHERE role_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        try {
            preparedStatement.setLong(1, roleId);
            preparedStatement.executeUpdate();
        } finally {
            preparedStatement.close();
        }
    }

    // ==================== Row Mappers ====================

    private Role mapRowToRole(ResultSet resultSet) throws SQLException {
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
        Set<Permission> permissions = new HashSet<>();

        return Role.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .name(name)
                .permissions(permissions)
                .build();
    }
}
