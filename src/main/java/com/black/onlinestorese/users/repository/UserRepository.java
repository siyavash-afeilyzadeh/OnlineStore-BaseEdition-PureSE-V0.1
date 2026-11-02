package com.black.onlinestorese.users.repository;

import com.black.onlinestorese.users.entity.Role;
import com.black.onlinestorese.users.entity.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class UserRepository {

    // ==================== Database Configuration ====================

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/onlinestore_db";
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

    public User save(User user) {
        user.onCreated();

        String sql = "INSERT INTO users (version, deleted, created_at, updated_at, deleted_at, username, password, enabled) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, user.getVersion() != null ? user.getVersion() : null);
                    preparedStatement.setBoolean(2, user.isDeleted());
                    preparedStatement.setTimestamp(3, user.getCreatedAt() != null ? Timestamp.valueOf(user.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, user.getUpdatedAt() != null ? Timestamp.valueOf(user.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, user.getDeletedAt() != null ? Timestamp.valueOf(user.getDeletedAt()) : null);
                    preparedStatement.setString(6, user.getUsername());
                    preparedStatement.setString(7, user.getPassword());
                    preparedStatement.setBoolean(8, user.isEnabled());

                    int affectedRow = preparedStatement.executeUpdate();
                    if (affectedRow == 0) {
                        throw new SQLException("Creating user failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            user.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating user failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return user;
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

    public Optional<User> findById(Long id) {
        String sql = "SELECT id, version ,deleted, created_at, updated_at, deleted_at, username, password, enabled " +
                "FROM users WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapToRowUser(resultSet));
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

    public List<User> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, username, password, enabled " +
                "FROM users WHERE deleted = false ORDER BY id ASC";

        List<User> users = new ArrayList<>();
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            users.add(mapToRowUser(resultSet));
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

        return users;
    }

    // ==================== Update ====================

    public void update(User user) {
        user.onUpdated();

        String sql = "UPDATE users SET version = ?, updated_at = ?, username = ?, password = ?, enabled = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, user.getVersion() != null ? user.getVersion() : null);
                    preparedStatement.setTimestamp(2, user.getUpdatedAt() != null ? Timestamp.valueOf(user.getUpdatedAt()) : null);
                    preparedStatement.setString(3, user.getUsername());
                    preparedStatement.setString(4, user.getPassword());
                    preparedStatement.setBoolean(5, user.isEnabled());
                    preparedStatement.setLong(6, user.getId());

                    int updated = preparedStatement.executeUpdate();
                    if(updated == 0) {
                        throw new SQLException("User update failed: record not found or marked as deleted.");
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
        User user = new User();
        user.onDeleted();

        String sql = "UPDATE users SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";
        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, user.getDeletedAt() != null ? Timestamp.valueOf(user.getDeletedAt()) : null);
                    preparedStatement.setLong(2, id);

                    int affected = preparedStatement.executeUpdate();
                    if(affected == 0) {
                        throw new SQLException("Soft delete failed: user not found or already deleted.");
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

    // ==================== Find By Username ====================

    public Optional<User> findByUsername (String username) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, username, password, enabled " +
                "FROM users WHERE username = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, username);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapToRowUser(resultSet));
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

    // ==================== Count By Role ====================

    public long countByRole(Long roleId) {
        String sql = "SELECT COUNT(DISTINCT u.id) FROM users u " +
                "JOIN user_roles ur ON ur.user_id = u.id " +
                "WHERE ur.role_id = ? AND u.deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, roleId);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return resultSet.getLong(1);
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
            throw new RuntimeException("Database error in countByRole(): " + e.getMessage(), e);
        }

        return 0L;
    }

    // ==================== Helper Methods for Roles Association ====================
    private Set<Role> findRolesByUserId(Connection connection, Long userId) throws SQLException {
        String sql = "SELECT r.id, r.version, r.deleted, r.created_at, r.updated_at, r.deleted_at, r.name " +
                "FROM roles r JOIN user_roles ur ON ur.role_id = r.id " +
                "WHERE ur.user_id = ? AND r.deleted = false ORDER BY r.id ASC";

        Set<Role> roles = new LinkedHashSet<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Role role = new Role();
                    role.setId(rs.getLong("id"));
                    role.setVersion(rs.getLong("version"));
                    role.setDeleted(rs.getBoolean("deleted"));

                    Timestamp createdTs = rs.getTimestamp("created_at");
                    role.setCreatedAt(createdTs != null ? createdTs.toLocalDateTime() : null);

                    Timestamp updatedTs = rs.getTimestamp("updated_at");
                    role.setUpdatedAt(updatedTs != null ? updatedTs.toLocalDateTime() : null);

                    Timestamp deletedTs = rs.getTimestamp("deleted_at");
                    role.setDeletedAt(deletedTs != null ? deletedTs.toLocalDateTime() : null);

                    role.setName(rs.getString("name"));
                    roles.add(role);
                }
            }
        }
        return roles;
    }

    private void insertRoleAssociations(Connection connection, Long userId, Set<Role> roles) throws SQLException {
        if (roles == null || roles.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Role role : roles) {
                if (role.getId() != null) {
                    ps.setLong(1, userId);
                    ps.setLong(2, role.getId());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private void deleteRoleAssociations(Connection connection, Long userId) throws SQLException {
        String sql = "DELETE FROM user_roles WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    // ==================== Row Mappers ====================

    private User mapToRowUser(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp deletedTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedTs != null) ? deletedTs.toLocalDateTime() : null;

        String username = resultSet.getString("username");
        String password = resultSet.getString("password");
        boolean enabled = resultSet.getBoolean("enabled");
        Set<Role> roles = new HashSet<>();

        return User.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .username(username)
                .password(password)
                .enabled(enabled)
                .roles(roles)
                .profile(null)
                .build();
    }
}
