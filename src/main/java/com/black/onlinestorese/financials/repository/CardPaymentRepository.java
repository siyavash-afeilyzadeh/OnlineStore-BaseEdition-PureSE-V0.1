package com.black.onlinestorese.financials.repository;

import com.black.onlinestorese.financials.entity.CardPayment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardPaymentRepository {

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

    public CardPayment save(CardPayment cardPayment) {
        cardPayment.onCreated();

        String sql = "INSERT INTO card_payments (version, deleted, created_at, updated_at, deleted_at, card_last_digits, tracking_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, cardPayment.getVersion() != null ? cardPayment.getVersion() : 0L);
                    preparedStatement.setBoolean(2, cardPayment.isDeleted());
                    preparedStatement.setTimestamp(3, cardPayment.getCreatedAt() != null ? Timestamp.valueOf(cardPayment.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, cardPayment.getUpdatedAt() != null ? Timestamp.valueOf(cardPayment.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, cardPayment.getDeletedAt() != null ? Timestamp.valueOf(cardPayment.getDeletedAt()) : null);
                    preparedStatement.setString(6, cardPayment.getCardLastDigits());
                    preparedStatement.setString(7, cardPayment.getTrackingCode());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating card payment failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            cardPayment.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating card payment failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return cardPayment;
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

    public Optional<CardPayment> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, card_last_digits, tracking_code " +
                "FROM card_payments WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToCardPayment(resultSet));
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

    public List<CardPayment> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, card_last_digits, tracking_code " +
                "FROM card_payments WHERE deleted = false ORDER BY id ASC";

        List<CardPayment> cardPayments = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            cardPayments.add(mapRowToCardPayment(resultSet));
                        }
                        return cardPayments;
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

    public CardPayment update(CardPayment cardPayment) {
        cardPayment.onUpdated();

        String sql = "UPDATE card_payments SET version = ?, updated_at = ?, card_last_digits = ?, tracking_code = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, cardPayment.getVersion() != null ? cardPayment.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, cardPayment.getUpdatedAt() != null ? Timestamp.valueOf(cardPayment.getUpdatedAt()) : null);
                    preparedStatement.setString(3, cardPayment.getCardLastDigits());
                    preparedStatement.setString(4, cardPayment.getTrackingCode());
                    preparedStatement.setLong(5, cardPayment.getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Card payment update failed, record not found or marked as deleted.");
                    }

                    cardPayment.setVersion(cardPayment.getVersion() != null ? cardPayment.getVersion() + 1 : 1L);
                    return cardPayment;
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
        CardPayment cardPayment = new CardPayment();
        cardPayment.onDeleted();

        String sql = "UPDATE card_payments SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, cardPayment.getDeletedAt() != null ? Timestamp.valueOf(cardPayment.getDeletedAt()) : null);
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

    // ==================== Row Mappers ====================

    private CardPayment mapRowToCardPayment(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null;

        Timestamp updatedAtTimestamp = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = updatedAtTimestamp != null ? updatedAtTimestamp.toLocalDateTime() : null;

        Timestamp deletedAtTimestamp = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = deletedAtTimestamp != null ? deletedAtTimestamp.toLocalDateTime() : null;

        String cardLastDigits = resultSet.getString("card_last_digits");
        String trackingCode = resultSet.getString("tracking_code");

        return CardPayment.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .cardLastDigits(cardLastDigits)
                .trackingCode(trackingCode)
                .build();
    }
}