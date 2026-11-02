package com.black.onlinestorese.delivery.repository;

import com.black.onlinestorese.delivery.entity.Courier;
import com.black.onlinestorese.delivery.entity.Delivery;
import com.black.onlinestorese.delivery.entity.enums.DeliveryStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliveryRepository {

    // ==================== Database Configuration ====================
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/onlinestore_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres123";

    // ==================== Connection Management ====================
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // ==================== CRUD Operations ====================
    // ==================== Create/Save ====================
    public Delivery save(Delivery delivery) {
        if (delivery.getId() == null) {
            return insert(delivery);
        } else {
            return update(delivery);
        }
    }

    private Delivery insert(Delivery delivery) {
        delivery.onCreated();
        String sql = "INSERT INTO deliveries (version, deleted, created_at, updated_at, deleted_at, " +
                "address, status, tracking_number, send_date, receive_date, delivery_notes, courier_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, delivery.getVersion() != null ? delivery.getVersion() : 0L);
                    preparedStatement.setBoolean(2, delivery.isDeleted());
                    preparedStatement.setTimestamp(3, delivery.getCreatedAt() != null ? Timestamp.valueOf(delivery.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, delivery.getUpdatedAt() != null ? Timestamp.valueOf(delivery.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, delivery.getDeletedAt() != null ? Timestamp.valueOf(delivery.getDeletedAt()) : null);
                    preparedStatement.setString(6, delivery.getAddress());
                    preparedStatement.setString(7, delivery.getStatus() != null ? delivery.getStatus().name() : DeliveryStatus.PREPARING.name());
                    preparedStatement.setString(8, delivery.getTrackingNumber());
                    preparedStatement.setTimestamp(9, delivery.getSendDate() != null ? Timestamp.valueOf(delivery.getSendDate()) : null);
                    preparedStatement.setTimestamp(10, delivery.getReceiveDate() != null ? Timestamp.valueOf(delivery.getReceiveDate()) : null);
                    preparedStatement.setString(11, delivery.getDeliveryNote());
                    preparedStatement.setLong(12, delivery.getCourier() != null ? delivery.getCourier().getId() : 0L);

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating delivery failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            delivery.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating delivery failed, no ID obtained.");
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

        return delivery;
    }

    // ==================== Read/Find By ID ====================
    public Optional<Delivery> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, " +
                "address, status, tracking_number, send_date, receive_date, delivery_notes, courier_id " +
                "FROM deliveries WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToDelivery(resultSet));
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
    public List<Delivery> findAll() {
        List<Delivery> deliveries = new ArrayList<>();
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, " +
                "address, status, tracking_number, send_date, receive_date, delivery_notes, courier_id " +
                "FROM deliveries WHERE deleted = false ORDER BY id ASC";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            deliveries.add(mapRowToDelivery(resultSet));
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

        return deliveries;
    }

    // ==================== Update ====================
    private Delivery update(Delivery delivery) {
        delivery.onUpdated();
        String sql = "UPDATE deliveries SET version = ?, deleted = ?, created_at = ?, updated_at = ?, deleted_at = ?, " +
                "address = ?, status = ?, tracking_number = ?, send_date = ?, receive_date = ?, delivery_notes = ?, courier_id = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, delivery.getVersion() != null ? delivery.getVersion() + 1 : 1L);
                    preparedStatement.setBoolean(2, delivery.isDeleted());
                    preparedStatement.setTimestamp(3, delivery.getCreatedAt() != null ? Timestamp.valueOf(delivery.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, delivery.getUpdatedAt() != null ? Timestamp.valueOf(delivery.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, delivery.getDeletedAt() != null ? Timestamp.valueOf(delivery.getDeletedAt()) : null);
                    preparedStatement.setString(6, delivery.getAddress());
                    preparedStatement.setString(7, delivery.getStatus() != null ? delivery.getStatus().name() : DeliveryStatus.PREPARING.name());
                    preparedStatement.setString(8, delivery.getTrackingNumber());
                    preparedStatement.setTimestamp(9, delivery.getSendDate() != null ? Timestamp.valueOf(delivery.getSendDate()) : null);
                    preparedStatement.setTimestamp(10, delivery.getReceiveDate() != null ? Timestamp.valueOf(delivery.getReceiveDate()) : null);
                    preparedStatement.setString(11, delivery.getDeliveryNote());
                    preparedStatement.setLong(12, delivery.getCourier() != null ? delivery.getCourier().getId() : 0L);
                    preparedStatement.setLong(13, delivery.getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Delivery update failed, record not found or marked as deleted.");
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

        return delivery;
    }

    // ==================== Delete (Soft Delete) ====================
    public void softDelete(Long id) {
        Delivery delivery = new Delivery();
        delivery.onDeleted();
        String sql = "UPDATE deliveries SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, delivery.getDeletedAt() != null ? Timestamp.valueOf(delivery.getDeletedAt()) : null);
                    preparedStatement.setLong(2, id);
                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Soft delete failed: delivery not found or already deleted.");
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

    // ==================== Find By Tracking Number ====================
    public Optional<Delivery> findByTrackingNumber(String trackingNumber) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, " +
                "address, status, tracking_number, send_date, receive_date, delivery_notes, courier_id " +
                "FROM deliveries WHERE tracking_number = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, trackingNumber);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToDelivery(resultSet));
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
            throw new RuntimeException("Database error in findByTrackingNumber(): " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    // ==================== Find By Courier ====================
    public List<Delivery> findByCourier(Long courierId) {
        List<Delivery> deliveries = new ArrayList<>();
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, " +
                "address, status, tracking_number, send_date, receive_date, delivery_notes, courier_id " +
                "FROM deliveries WHERE courier_id = ? AND deleted = false ORDER BY id ASC";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, courierId);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            deliveries.add(mapRowToDelivery(resultSet));
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
            throw new RuntimeException("Database error in findByCourier(): " + e.getMessage(), e);
        }

        return deliveries;
    }

    // ==================== Count By Courier ====================
    public Long countByCourier(Long courierId) {
        String sql = "SELECT COUNT(*) FROM deliveries WHERE courier_id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, courierId);
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
            throw new RuntimeException("Database error in countByCourier(): " + e.getMessage(), e);
        }

        return 0L;
    }

    // ==================== Row Mappers ====================
    private Delivery mapRowToDelivery(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdAtTs != null) ? createdAtTs.toLocalDateTime() : null;

        Timestamp updatedAtTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedAtTs != null) ? updatedAtTs.toLocalDateTime() : null;

        Timestamp deletedAtTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedAtTs != null) ? deletedAtTs.toLocalDateTime() : null;

        String address = resultSet.getString("address");

        String statusStr = resultSet.getString("status");
        DeliveryStatus status = (statusStr != null) ? DeliveryStatus.valueOf(statusStr) : DeliveryStatus.PREPARING;

        String trackingNumber = resultSet.getString("tracking_number");

        Timestamp sendDateTs = resultSet.getTimestamp("send_date");
        LocalDateTime sendDate = (sendDateTs != null) ? sendDateTs.toLocalDateTime() : null;

        Timestamp receiveDateTs = resultSet.getTimestamp("receive_date");
        LocalDateTime receiveDate = (receiveDateTs != null) ? receiveDateTs.toLocalDateTime() : null;

        String deliveryNote = resultSet.getString("delivery_notes");

        Long courierId = resultSet.getLong("courier_id");
        Courier courier = new Courier();
        courier.setId(courierId);

        Delivery delivery = Delivery.builder()
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .address(address)
                .status(status)
                .trackingNumber(trackingNumber)
                .sendDate(sendDate)
                .receiveDate(receiveDate)
                .deliveryNote(deliveryNote)
                .courier(courier)
                .build();

        delivery.setId(id);
        return delivery;
    }
}
