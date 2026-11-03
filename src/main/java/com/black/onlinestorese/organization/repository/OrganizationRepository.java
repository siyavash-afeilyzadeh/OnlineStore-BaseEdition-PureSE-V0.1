package com.black.onlinestorese.organization.repository;

import com.black.onlinestorese.organization.entity.Organization;
import com.black.onlinestorese.organization.entity.enums.OrganizationStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrganizationRepository {

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

    public Organization save(Organization organization) {
        organization.onCreated();

        String sql = "INSERT INTO organizations (version, deleted, created_at, updated_at, deleted_at, name, registration_number, email, website, establishment_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, organization.getVersion() != null ? organization.getVersion() : 0L);
                    preparedStatement.setBoolean(2, organization.isDeleted());
                    preparedStatement.setTimestamp(3, organization.getCreatedAt() != null ? Timestamp.valueOf(organization.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, organization.getUpdatedAt() != null ? Timestamp.valueOf(organization.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, organization.getDeletedAt() != null ? Timestamp.valueOf(organization.getDeletedAt()) : null);
                    preparedStatement.setString(6, organization.getName());
                    preparedStatement.setString(7, organization.getRegistrationNumber());
                    preparedStatement.setString(8, organization.getEmail());
                    preparedStatement.setString(9, organization.getWebsite());
                    preparedStatement.setDate(10, organization.getEstablishmentDate() != null ? Date.valueOf(organization.getEstablishmentDate()) : null);
                    preparedStatement.setString(11, organization.getStatus() != null ? organization.getStatus().name() : OrganizationStatus.ACTIVE.name());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating organization failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            organization.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating organization failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return organization;
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

    public Optional<Organization> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, name, registration_number, email, website, establishment_date, status " +
                "FROM organizations WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToOrganization(resultSet));
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

    public List<Organization> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, name, registration_number, email, website, establishment_date, status " +
                "FROM organizations WHERE deleted = false ORDER BY id ASC";

        List<Organization> organizations = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            organizations.add(mapRowToOrganization(resultSet));
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

        return organizations;
    }

    // ==================== Update ====================

    public void update(Organization organization) {
        organization.onUpdated();

        String sql = "UPDATE organizations SET version = ?, updated_at = ?, name = ?, registration_number = ?, email = ?, website = ?, establishment_date = ?, status = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, organization.getVersion() != null ? organization.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(organization.getUpdatedAt()));
                    preparedStatement.setString(3, organization.getName());
                    preparedStatement.setString(4, organization.getRegistrationNumber());
                    preparedStatement.setString(5, organization.getEmail());
                    preparedStatement.setString(6, organization.getWebsite());
                    preparedStatement.setDate(7, organization.getEstablishmentDate() != null ? Date.valueOf(organization.getEstablishmentDate()) : null);
                    preparedStatement.setString(8, organization.getStatus() != null ? organization.getStatus().name() : null);
                    preparedStatement.setLong(9, organization.getId());

                    int updated = preparedStatement.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Organization update failed: record not found or marked as deleted.");
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
        Organization organization = new Organization();
        organization.onDeleted();

        String sql = "UPDATE organizations SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, Timestamp.valueOf(organization.getDeletedAt()));
                    preparedStatement.setLong(2, id);

                    int affected = preparedStatement.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Soft delete failed: organization not found or already deleted.");
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

    // ==================== Find By Registration Number ====================

    public Optional<Organization> findByRegistrationNumber(String registrationNumber) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, name, registration_number, email, website, establishment_date, status " +
                "FROM organizations WHERE registration_number = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, registrationNumber);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToOrganization(resultSet));
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
            throw new RuntimeException("Database error in findByRegistrationNumber(): " + e.getMessage(), e);
        }
    }

    // ==================== Row Mappers ====================

    private Organization mapRowToOrganization(ResultSet resultSet) throws SQLException {
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
        String registrationNumber = resultSet.getString("registration_number");
        String email = resultSet.getString("email");
        String website = resultSet.getString("website");

        Date establishmentSqlDate = resultSet.getDate("establishment_date");
        LocalDate establishmentDate = (establishmentSqlDate != null) ? establishmentSqlDate.toLocalDate() : null;

        String statusStr = resultSet.getString("status");
        OrganizationStatus status = (statusStr != null) ? OrganizationStatus.valueOf(statusStr) : null;

        return Organization.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .name(name)
                .registrationNumber(registrationNumber)
                .email(email)
                .website(website)
                .establishmentDate(establishmentDate)
                .status(status)
                .build();
    }
}
