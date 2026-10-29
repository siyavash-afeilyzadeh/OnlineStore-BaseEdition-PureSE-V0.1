package com.black.onlinestorese.tickets.repository;

import com.black.onlinestorese.tickets.entity.Ticket;
import com.black.onlinestorese.tickets.entity.TicketMessage;
import com.black.onlinestorese.users.entity.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketMessageRepository {
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

    public TicketMessage save(TicketMessage ticketMessage) {
        ticketMessage.onCreated();

        String sql = "INSERT INTO ticket_messages (version, deleted, created_at, updated_at, deleted_at, ticket_id, sender_id, content) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, ticketMessage.getVersion() != null ? ticketMessage.getVersion() : 0L);
                    preparedStatement.setBoolean(2, ticketMessage.isDeleted());
                    preparedStatement.setTimestamp(3, ticketMessage.getCreatedAt() != null ? Timestamp.valueOf(ticketMessage.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, ticketMessage.getUpdatedAt() != null ? Timestamp.valueOf(ticketMessage.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, ticketMessage.getDeletedAt() != null ? Timestamp.valueOf(ticketMessage.getDeletedAt()) : null);
                    preparedStatement.setLong(6, ticketMessage.getTicket().getId());
                    preparedStatement.setLong(7, ticketMessage.getSender().getId());
                    preparedStatement.setString(8, ticketMessage.getContent());

                    int affectedRow = preparedStatement.executeUpdate();
                    if (affectedRow == 0) {
                        throw new SQLException("Creating ticket message failed, no rows affected.");
                    }

                    ResultSet resultSet = preparedStatement.getGeneratedKeys();
                    try {
                        if (resultSet.next()) {
                            ticketMessage.setId(resultSet.getLong(1));
                        } else {
                            throw new SQLException("Creating ticket message failed, no ID obtained.");
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
            throw new RuntimeException("Database error in save(): " + e.getMessage(), e);
        }

        return ticketMessage;
    }

    // ==================== Read/Find By ID ====================

    public Optional<TicketMessage> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, ticket_id, sender_id, content " +
                "FROM ticket_messages WHERE id = ? AND deleted= false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToTicketMessage(resultSet));
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

    public List<TicketMessage> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, ticket_id, sender_id, content " +
                "FROM ticket_messages WHERE deleted = false ORDER BY id ASC";

        List<TicketMessage> messages = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            messages.add(mapRowToTicketMessage(resultSet));
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

        return messages;
    }

    // ==================== Update ====================

    public TicketMessage update(TicketMessage ticketMessage) {
        ticketMessage.onUpdated();

        String sql = "UPDATE ticket_messages SET version = ?, updated_at = ?, content = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    Long newVersion = ticketMessage.getVersion() != null ? ticketMessage.getVersion() + 1 : 1L;

                    preparedStatement.setLong(1, newVersion);
                    preparedStatement.setTimestamp(2, ticketMessage.getUpdatedAt() != null ? Timestamp.valueOf(ticketMessage.getUpdatedAt()) : null);
                    preparedStatement.setString(3, ticketMessage.getContent());
                    preparedStatement.setLong(4, ticketMessage.getId());

                    int updatedRows = preparedStatement.executeUpdate();
                    if (updatedRows == 0) {
                        throw new SQLException("Updating ticket message failed, no rows affected.");
                    }

                    ticketMessage.setVersion(newVersion);
                } finally {
                    preparedStatement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in update(): " + e.getMessage(), e);
        }

        return ticketMessage;
    }

    // ==================== Delete (Soft Delete) ====================

    public void softDelete(Long id) {
        TicketMessage ticketMessage = new TicketMessage();
        ticketMessage.onDeleted();

        String sql = "UPDATE ticket_messages SET deleted = ?, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setBoolean(1, ticketMessage.isDeleted());
                    preparedStatement.setTimestamp(2, ticketMessage.getDeletedAt() != null ? Timestamp.valueOf(ticketMessage.getDeletedAt()) : null);
                    preparedStatement.setLong(3, id);

                    int updatedRows = preparedStatement.executeUpdate();
                    if (updatedRows == 0) {
                        throw new SQLException("Soft deleting ticket message failed, no rows affected.");
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

    // ==================== Find By Ticket ====================

    public List<TicketMessage> findByTicket(Long ticketId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, ticket_id, sender_id, content " +
                "FROM ticket_messages WHERE ticket_id = ? AND deleted = false ORDER BY id ASC";

        List<TicketMessage> messages = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, ticketId);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            messages.add(mapRowToTicketMessage(resultSet));
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
            throw new RuntimeException("Database error in findByTicket(): " + e.getMessage(), e);
        }

        return messages;
    }

    // ==================== Row Mappers ====================

    private TicketMessage mapRowToTicketMessage(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        Boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp deletedTS = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedTS != null) ? deletedTS.toLocalDateTime() : null;

        Long ticketId = resultSet.getLong("ticket_id");
        Long senderId = resultSet.getLong("sender_id");
        String content = resultSet.getString("content");

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);

        User sender = new User();
        sender.setId(senderId);

        return TicketMessage.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .ticket(ticket)
                .sender(sender)
                .content(content)
                .build();
    }
}
