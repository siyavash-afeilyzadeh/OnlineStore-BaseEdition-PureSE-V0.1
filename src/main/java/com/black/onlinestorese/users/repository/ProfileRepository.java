package com.black.onlinestorese.users.repository;

import com.black.onlinestorese.users.entity.Profile;
import com.black.onlinestorese.users.entity.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfileRepository {

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

    public Profile save(Profile profile) throws SQLException {
        profile.onCreated();

        String sql = "INSERT INTO profiles (version, deleted, created_at, updated_at, deleted_at, first_name, last_name, phone_number, birth_date, address, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, profile.getVersion() != null ? profile.getVersion() : 0L);
                    preparedStatement.setBoolean(2, profile.isDeleted());
                    preparedStatement.setTimestamp(3, profile.getCreatedAt() != null ? Timestamp.valueOf(profile.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, profile.getUpdatedAt() != null ? Timestamp.valueOf(profile.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, profile.getDeletedAt() != null ? Timestamp.valueOf(profile.getDeletedAt()) : null);
                    preparedStatement.setString(6, profile.getFirstName());
                    preparedStatement.setString(7, profile.getLastName());
                    preparedStatement.setString(8, profile.getPhoneNumber());
                    preparedStatement.setDate(9, profile.getBirthDate() != null ? Date.valueOf(profile.getBirthDate()) : null);
                    preparedStatement.setString(10, profile.getAddress());
                    preparedStatement.setLong(11, profile.getUser().getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating profile failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if(generatedKeys.next()){
                            profile.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating profile failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return profile;
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

    public Optional<Profile> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, first_name, last_name, phone_number, birth_date, address, user_id " +
                "FROM profiles WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToProfile(resultSet));
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

    public List<Profile> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at,first_name, last_name, phone_number, birth_date, address, user_id " +
                "FROM profiles WHERE deleted = false ORDER BY id ASC";

        List<Profile> profiles = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()){
                            profiles.add(mapRowToProfile(resultSet));
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

        return profiles;
    }

    // ==================== Update ====================

    public void update(Profile profile) {
        profile.onUpdated();

        String sql = "UPDATE profiles SET version = ?, updated_at = ?, first_name = ?, last_name = ?, phone_number = ?, birth_date = ?, address = ?, user_id = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, profile.getVersion() != null ? profile.getVersion() : null);
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(profile.getUpdatedAt()));
                    preparedStatement.setString(3, profile.getFirstName());
                    preparedStatement.setString(4, profile.getLastName());
                    preparedStatement.setString(5, profile.getPhoneNumber());
                    preparedStatement.setDate(6, Date.valueOf(profile.getBirthDate()));
                    preparedStatement.setString(7, profile.getAddress());
                    preparedStatement.setLong(8, profile.getUser().getId());
                    preparedStatement.setLong(9, profile.getId());

                    int updated = preparedStatement.executeUpdate();
                    if(updated == 0){
                        throw new SQLException("Profile update failed: record not found or marked as deleted.");
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

    public void delete(Long id) {
        Profile profile = new Profile();
        profile.onDeleted();

        String sql = "UPDATE profiles SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, Timestamp.valueOf(profile.getDeletedAt()));
                    preparedStatement.setLong(2, id);

                    int affected = preparedStatement.executeUpdate();
                    if(affected == 0) {
                        throw new SQLException("Soft delete failed: profile not found or already deleted.");
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

    // ==================== Find By User ====================

    public Optional<Profile> findByUser(Long userId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, first_name, last_name, phone_number, birth_date, address, user_id " +
                "FROM profiles WHERE user_id = ? AND deleted = false ORDER BY id ASC";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1 , userId);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if(resultSet.next()) {
                            return Optional.of(mapRowToProfile(resultSet));
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
            throw new RuntimeException("Database error in findByUserId(): " + e.getMessage(), e);
        }
    }

    // ==================== Row Mappers ====================

    private Profile mapRowToProfile(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp deletedTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedTs != null) ? deletedTs.toLocalDateTime() : null;

        String firstName = resultSet.getString("first_name");
        String lastName = resultSet.getString("last_name");
        String phoneNumber = resultSet.getString("phone_number");

        Date birthD = resultSet.getDate("birth_date");
        LocalDate birthDate = (birthD != null) ? birthD.toLocalDate() : null;

        String address = resultSet.getString("address");
        Long userId = resultSet.getLong("user_id");

        User user = new User();
        user.setId(userId);

        return Profile.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .birthDate(birthDate)
                .address(address)
                .user(user)
                .build();
    }
}
