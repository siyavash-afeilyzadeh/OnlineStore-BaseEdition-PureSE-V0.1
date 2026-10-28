package com.black.onlinestorese.tickets.repository;

import com.black.onlinestorese.tickets.entity.Ticket;
import com.black.onlinestorese.tickets.entity.enums.TicketStatus;
import com.black.onlinestorese.users.entity.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketRepository {

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

    public Ticket save(Ticket ticket) {
        ticket.onCreated();

        String sql = "INSERT INTO tickets (version, deleted, created_at, updated_at, deleted_at, subject, user_id, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, ticket.getVersion() != null ? ticket.getVersion() : 0L);
                    preparedStatement.setBoolean(2, ticket.isDeleted());
                    preparedStatement.setTimestamp(3, ticket.getCreatedAt() != null ? Timestamp.valueOf(ticket.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, ticket.getUpdatedAt() != null ? Timestamp.valueOf(ticket.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, ticket.getDeletedAt() != null ? Timestamp.valueOf(ticket.getDeletedAt()) : null);
                    preparedStatement.setString(6, ticket.getSubject());
                    preparedStatement.setLong(7, ticket.getUser().getId());
                    preparedStatement.setString(8, ticket.getStatus().name());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating ticket failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            ticket.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating ticket failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return ticket;
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

    public Optional<Ticket> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, subject, user_id, status " +
                "FROM tickets WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToTicket(resultSet));
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

    public List<Ticket> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, subject, user_id, status " +
                "FROM tickets WHERE deleted = false ORDER BY id ASC";

        List<Ticket> tickets = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            tickets.add(mapRowToTicket(resultSet));
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

        return tickets;
    }

    // ==================== Update ====================

    public void update(Ticket ticket) {
        ticket.onUpdated();

        String sql = "UPDATE tickets SET version = ?, updated_at = ?, subject = ?, status = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, ticket.getVersion() != null ? ticket.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(ticket.getUpdatedAt()));
                    preparedStatement.setString(3, ticket.getSubject());
                    preparedStatement.setString(4, ticket.getStatus().name());
                    preparedStatement.setLong(5, ticket.getId());

                    int updated = preparedStatement.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Ticket update failed: record not found or marked as deleted.");
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
        Ticket ticket = new Ticket();
        ticket.onDeleted();

        String sql = "UPDATE tickets SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, Timestamp.valueOf(ticket.getDeletedAt()));
                    preparedStatement.setLong(2, id);

                    int affected = preparedStatement.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Soft delete failed: ticket not found or already deleted.");
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

    public List<Ticket> findByUser(Long userId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, subject, user_id, status " +
                "FROM tickets WHERE user_id = ? AND deleted = false ORDER BY created_at DESC";

        List<Ticket> tickets = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, userId);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            tickets.add(mapRowToTicket(resultSet));
                        }
                    } finally {
                        resultSet.close();
                    }

                    return tickets;
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findAllByUserId(): " + e.getMessage(), e);
        }
    }

    // ==================== Row Mappers ====================

    private Ticket mapRowToTicket(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp deletedTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedTs != null) ? deletedTs.toLocalDateTime() : null;

        String subject = resultSet.getString("subject");
        Long userId = resultSet.getLong("user_id");
        String statusStr = resultSet.getString("status");

        TicketStatus status = (statusStr != null) ? TicketStatus.valueOf(statusStr) : TicketStatus.OPEN;

        User user = new User();
        user.setId(userId);

        return Ticket.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .subject(subject)
                .user(user)
                .status(status)
                .build();
    }
}
