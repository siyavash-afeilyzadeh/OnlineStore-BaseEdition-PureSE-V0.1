package com.black.onlinestorese.attachments.repository;

import com.black.onlinestorese.attachments.entity.Attachment;
import com.black.onlinestorese.attachments.entity.enums.AttachmentOwnerType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttachmentRepository {

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
    public Attachment save(Attachment attachment) {
        if (attachment.getId() == null) {
            return insert(attachment);
        } else {
            return update(attachment);
        }
    }

    private Attachment insert(Attachment attachment) {
        attachment.onCreated();
        String sql = "INSERT INTO attachments (version, deleted, created_at, updated_at, deleted_at, " +
                "owner_type, owner_id, title, file_name, description, keywords, content_type, file_size, stored_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, attachment.getVersion() != null ? attachment.getVersion() : 0L);
                    preparedStatement.setBoolean(2, attachment.getDeleted());
                    preparedStatement.setTimestamp(3, attachment.getCreatedAt() != null ? Timestamp.valueOf(attachment.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, attachment.getUpdatedAt() != null ? Timestamp.valueOf(attachment.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, attachment.getDeletedAt() != null ? Timestamp.valueOf(attachment.getDeletedAt()) : null);
                    preparedStatement.setString(6, attachment.getOwnerType() != null ? attachment.getOwnerType().name() : null);
                    preparedStatement.setLong(7, attachment.getOwnerId() != null ? attachment.getOwnerId() : 0L);
                    preparedStatement.setString(8, attachment.getTitle());
                    preparedStatement.setString(9, attachment.getFileName());
                    preparedStatement.setString(10, attachment.getDescription());
                    preparedStatement.setString(11, attachment.getKeywords());
                    preparedStatement.setString(12, attachment.getContentType());
                    preparedStatement.setObject(13, attachment.getFileSize());
                    preparedStatement.setString(14, attachment.getStoredName());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating attachment failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            attachment.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating attachment failed, no ID obtained.");
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

        return attachment;
    }

    // ==================== Read/Find By ID ====================
    public Optional<Attachment> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, " +
                "owner_type, owner_id, title, file_name, description, keywords, content_type, file_size, stored_name " +
                "FROM attachments WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToAttachment(resultSet));
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
    public List<Attachment> findAll() {
        List<Attachment> attachments = new ArrayList<>();
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, " +
                "owner_type, owner_id, title, file_name, description, keywords, content_type, file_size, stored_name " +
                "FROM attachments WHERE deleted = false ORDER BY id ASC";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            attachments.add(mapRowToAttachment(resultSet));
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

        return attachments;
    }

    // ==================== Update ====================
    private Attachment update(Attachment attachment) {
        attachment.onUpdated();
        String sql = "UPDATE attachments SET version = ?, deleted = ?, created_at = ?, updated_at = ?, deleted_at = ?, " +
                "owner_type = ?, owner_id = ?, title = ?, file_name = ?, description = ?, keywords = ?, content_type = ?, file_size = ?, stored_name = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, attachment.getVersion() != null ? attachment.getVersion() + 1 : 1L);
                    preparedStatement.setBoolean(2, attachment.getDeleted());
                    preparedStatement.setTimestamp(3, attachment.getCreatedAt() != null ? Timestamp.valueOf(attachment.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, attachment.getUpdatedAt() != null ? Timestamp.valueOf(attachment.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, attachment.getDeletedAt() != null ? Timestamp.valueOf(attachment.getDeletedAt()) : null);
                    preparedStatement.setString(6, attachment.getOwnerType() != null ? attachment.getOwnerType().name() : null);
                    preparedStatement.setLong(7, attachment.getOwnerId() != null ? attachment.getOwnerId() : 0L);
                    preparedStatement.setString(8, attachment.getTitle());
                    preparedStatement.setString(9, attachment.getFileName());
                    preparedStatement.setString(10, attachment.getDescription());
                    preparedStatement.setString(11, attachment.getKeywords());
                    preparedStatement.setString(12, attachment.getContentType());
                    preparedStatement.setObject(13, attachment.getFileSize());
                    preparedStatement.setString(14, attachment.getStoredName());
                    preparedStatement.setLong(15, attachment.getId());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Updating attachment failed, record not found or marked as deleted.");
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

        return attachment;
    }

    // ==================== Delete (Soft Delete) ====================
    public void softDelete(Long id) {
        Attachment attachment = new Attachment();
        attachment.onDeleted();
        String sql = "UPDATE attachments SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, attachment.getDeletedAt() != null ? Timestamp.valueOf(attachment.getDeletedAt()) : null);
                    preparedStatement.setLong(2, id);
                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Soft delete failed: attachment not found or already deleted.");
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

    // ==================== Find By Owner ====================
    public List<Attachment> findByOwner(AttachmentOwnerType ownerType, Long ownerId) {
        List<Attachment> attachments = new ArrayList<>();
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, " +
                "owner_type, owner_id, title, file_name, description, keywords, content_type, file_size, stored_name " +
                "FROM attachments WHERE owner_type = ? AND owner_id = ? AND deleted = false ORDER BY id ASC";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, ownerType != null ? ownerType.name() : null);
                    preparedStatement.setLong(2, ownerId != null ? ownerId : 0L);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            attachments.add(mapRowToAttachment(resultSet));
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
            throw new RuntimeException("Database error in findByOwner(): " + e.getMessage(), e);
        }

        return attachments;
    }

    // ==================== Find By Owner And File Name ====================
    public Optional<Attachment> findByOwnerAndFileName(AttachmentOwnerType ownerType, Long ownerId, String fileName) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, " +
                "owner_type, owner_id, title, file_name, description, keywords, content_type, file_size, stored_name " +
                "FROM attachments WHERE owner_type = ? AND owner_id = ? AND file_name = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, ownerType != null ? ownerType.name() : null);
                    preparedStatement.setLong(2, ownerId != null ? ownerId : 0L);
                    preparedStatement.setString(3, fileName);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToAttachment(resultSet));
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
            throw new RuntimeException("Database error in findByOwnerAndFileName(): " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    // ==================== Row Mappers ====================
    private Attachment mapRowToAttachment(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdAtTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdAtTs != null) ? createdAtTs.toLocalDateTime() : null;

        Timestamp updatedAtTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedAtTs != null) ? updatedAtTs.toLocalDateTime() : null;

        Timestamp deletedAtTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedAtTs != null) ? deletedAtTs.toLocalDateTime() : null;

        String ownerTypeStr = resultSet.getString("owner_type");
        AttachmentOwnerType ownerType = (ownerTypeStr != null && !ownerTypeStr.isEmpty()) ? AttachmentOwnerType.valueOf(ownerTypeStr) : null;

        Long ownerId = resultSet.getLong("owner_id");
        String title = resultSet.getString("title");
        String fileName = resultSet.getString("file_name");
        String description = resultSet.getString("description");
        String keywords = resultSet.getString("keywords");
        String contentType = resultSet.getString("content_type");
        Long fileSize = resultSet.getObject("file_size", Long.class);
        String storedName = resultSet.getString("stored_name");

        return Attachment.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .ownerType(ownerType)
                .ownerId(ownerId)
                .title(title)
                .fileName(fileName)
                .description(description)
                .keywords(keywords)
                .contentType(contentType)
                .fileSize(fileSize)
                .storedName(storedName)
                .build();
    }
}
