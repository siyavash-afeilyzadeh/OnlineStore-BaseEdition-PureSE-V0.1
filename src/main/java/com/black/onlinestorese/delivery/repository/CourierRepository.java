package com.black.onlinestorese.delivery.repository;

import com.black.onlinestorese.delivery.entity.Courier;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourierRepository {

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
    public Courier save(Courier courier) {
        if (courier.getId() == null) {
            return insert(courier);
        } else {
            return update(courier);
        }
    }

    private Courier insert(Courier courier) {
        courier.onCreated();
        String sql = "INSERT INTO couriers (version, deleted, created_at, updated_at, deleted_at, " +
                "name, phone, email, vehicle_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, courier.getVersion() != null ? courier.getVersion() : 0L);
                    preparedStatement.setBoolean(2, courier.isDeleted());
                    preparedStatement.setTimestamp(3, courier.getCreatedAt() != null ? Timestamp.valueOf(courier.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, courier.getUpdatedAt() != null ? Timestamp.valueOf(courier.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, courier.getDeletedAt() != null ? Timestamp.valueOf(courier.getDeletedAt()) : null);
                    preparedStatement.setString(6, courier.getName());
                    preparedStatement.setString(7, courier.getPhone());
                    preparedStatement.setString(8, courier.getEmail());
                    preparedStatement.setString(9, courier.getVehicleType());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating courier failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            courier.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating courier failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in insert(): " + e.getMessage(), e);
        }

        return courier;
    }

    // ==================== Read/Find By ID ====================
    public Optional<Courier> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, " +
                "name, phone, email, vehicle_type FROM couriers WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToCourier(resultSet));
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
            throw new RuntimeException("Database error in findById(): " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    // ==================== Read/Find All ====================
    public List<Courier> findAll() {
        List<Courier> couriers = new ArrayList<>();
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, " +
                "name, phone, email, vehicle_type FROM couriers WHERE deleted = false ORDER BY id ASC";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            couriers.add(mapRowToCourier(resultSet));
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

        return couriers;
    }

    // ==================== Update ====================
    private Courier update(Courier courier) {
        courier.onUpdated();
        String sql = "UPDATE couriers SET version = ?, deleted = ?, created_at = ?, updated_at = ?, deleted_at = ?, " +
                "name = ?, phone = ?, email = ?, vehicle_type = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, courier.getVersion() != null ? courier.getVersion() + 1 : 1L);
                    preparedStatement.setBoolean(2, courier.isDeleted());
                    preparedStatement.setTimestamp(3, courier.getCreatedAt() != null ? Timestamp.valueOf(courier.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, courier.getUpdatedAt() != null ? Timestamp.valueOf(courier.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, courier.getDeletedAt() != null ? Timestamp.valueOf(courier.getDeletedAt()) : null);
                    preparedStatement.setString(6, courier.getName());
                    preparedStatement.setString(7, courier.getPhone());
                    preparedStatement.setString(8, courier.getEmail());
                    preparedStatement.setString(9, courier.getVehicleType());
                    preparedStatement.setLong(10, courier.getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Updating courier failed, record not found or marked as deleted.");
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

        return courier;
    }

    // ==================== Delete (Soft Delete) ====================
    public void softDelete(Long id) {
        Courier courier = new Courier();
        courier.onDeleted();
        String sql = "UPDATE couriers SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, courier.getDeletedAt() != null ? Timestamp.valueOf(courier.getDeletedAt()) : null);
                    preparedStatement.setLong(2, id);
                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Soft delete failed: courier not found or already deleted.");
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

    // ==================== Find By Email ====================
    public Optional<Courier> findByEmail(String email) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, " +
                "name, phone, email, vehicle_type FROM couriers WHERE email = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, email);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToCourier(resultSet));
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
            throw new RuntimeException("Database error in findByEmail(): " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    // ==================== Row Mappers ====================
    private Courier mapRowToCourier(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdAtTs != null) ? createdAtTs.toLocalDateTime() : null;

        Timestamp updatedAtTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedAtTs != null) ? updatedAtTs.toLocalDateTime() : null;

        Timestamp deletedAtTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedAtTs != null) ? deletedAtTs.toLocalDateTime() : null;

        String name = resultSet.getString("name");
        String phone = resultSet.getString("phone");
        String email = resultSet.getString("email");
        String vehicleType = resultSet.getString("vehicle_type");

        Courier courier = Courier.builder()
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .name(name)
                .phone(phone)
                .email(email)
                .vehicleType(vehicleType)
                .build();

        courier.setId(id);
        return courier;
    }
}
