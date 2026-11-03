package com.black.onlinestorese.organization.repository;

import com.black.onlinestorese.organization.entity.Branch;
import com.black.onlinestorese.organization.entity.Organization;
import com.black.onlinestorese.organization.entity.enums.BranchStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BranchRepository {

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

    public Branch save(Branch branch) {
        branch.onCreated();

        String sql = "INSERT INTO branches (version, deleted, created_at, updated_at, deleted_at, organization_id, name, branch_code, phone_number, email, website, opening_date, status, address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, branch.getVersion() != null ? branch.getVersion() : 0L);
                    preparedStatement.setBoolean(2, branch.isDeleted());
                    preparedStatement.setTimestamp(3, branch.getCreatedAt() != null ? Timestamp.valueOf(branch.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, branch.getUpdatedAt() != null ? Timestamp.valueOf(branch.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, branch.getDeletedAt() != null ? Timestamp.valueOf(branch.getDeletedAt()) : null);
                    preparedStatement.setLong(6, branch.getOrganization().getId());
                    preparedStatement.setString(7, branch.getName());
                    preparedStatement.setString(8, branch.getBranchCode());
                    preparedStatement.setString(9, branch.getPhoneNumber());
                    preparedStatement.setString(10, branch.getEmail());
                    preparedStatement.setString(11, branch.getWebsite());
                    preparedStatement.setObject(12, branch.getOpeningDate());
                    preparedStatement.setString(13, branch.getStatus() != null ? branch.getStatus().name() : BranchStatus.ACTIVE.name());
                    preparedStatement.setString(14, branch.getAddress());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating branch failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            branch.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating branch failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return branch;
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

    public Optional<Branch> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, organization_id, name, branch_code, phone_number, email, website, opening_date, status, address " +
                "FROM branches WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToBranch(resultSet));
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

    public List<Branch> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, organization_id, name, branch_code, phone_number, email, website, opening_date, status, address " +
                "FROM branches WHERE deleted = false ORDER BY id ASC";

        List<Branch> branches = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            branches.add(mapRowToBranch(resultSet));
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

        return branches;
    }

    // ==================== Update ====================

    public void update(Branch branch) {
        branch.onUpdated();

        String sql = "UPDATE branches SET version = ?, updated_at = ?, organization_id = ?, name = ?, branch_code = ?, phone_number = ?, email = ?, website = ?, opening_date = ?, status = ?, address = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, branch.getVersion() != null ? branch.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(branch.getUpdatedAt()));
                    preparedStatement.setLong(3, branch.getOrganization().getId());
                    preparedStatement.setString(4, branch.getName());
                    preparedStatement.setString(5, branch.getBranchCode());
                    preparedStatement.setString(6, branch.getPhoneNumber());
                    preparedStatement.setString(7, branch.getEmail());
                    preparedStatement.setString(8, branch.getWebsite());
                    preparedStatement.setObject(9, branch.getOpeningDate());
                    preparedStatement.setString(10, branch.getStatus() != null ? branch.getStatus().name() : null);
                    preparedStatement.setString(11, branch.getAddress());
                    preparedStatement.setLong(12, branch.getId());

                    int updated = preparedStatement.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Branch update failed: record not found or marked as deleted.");
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
        Branch branch = new Branch();
        branch.onDeleted();

        String sql = "UPDATE branches SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, Timestamp.valueOf(branch.getDeletedAt()));
                    preparedStatement.setLong(2, id);

                    int affected = preparedStatement.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Soft delete failed: branch not found or already deleted.");
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

    // ==================== Find By Organization ID ====================

    public List<Branch> findByOrganizationId(Long organizationId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, organization_id, name, branch_code, phone_number, email, website, opening_date, status, address " +
                "FROM branches WHERE organization_id = ? AND deleted = false ORDER BY id ASC";

        List<Branch> branches = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, organizationId);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            branches.add(mapRowToBranch(resultSet));
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
            throw new RuntimeException("Database error in findByOrganizationId(): " + e.getMessage(), e);
        }

        return branches;
    }

    // ==================== Count By Organization ID ====================

    public long countByOrganizationId(Long organizationId) {
        String sql = "SELECT count(id) FROM branches WHERE organization_id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, organizationId);

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
            throw new RuntimeException("Database error in countByOrganizationId(): " + e.getMessage(), e);
        }

        return 0L;
    }

    // ==================== Find By Branch Code ====================

    public Optional<Branch> findByBranchCode(String branchCode) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, organization_id, name, branch_code, phone_number, email, website, opening_date, status, address " +
                "FROM branches WHERE branch_code = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, branchCode);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToBranch(resultSet));
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
            throw new RuntimeException("Database error in findByBranchCode(): " + e.getMessage(), e);
        }
    }

    // ==================== Row Mappers ====================

    private Branch mapRowToBranch(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp deletedTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedTs != null) ? deletedTs.toLocalDateTime() : null;

        Long organizationId = resultSet.getLong("organization_id");
        String name = resultSet.getString("name");
        String branchCode = resultSet.getString("branch_code");
        String phoneNumber = resultSet.getString("phone_number");
        String email = resultSet.getString("email");
        String website = resultSet.getString("website");

        Date openingDateSql = resultSet.getDate("opening_date");
        LocalDate openingDate = (openingDateSql != null) ? openingDateSql.toLocalDate() : null;

        String statusStr = resultSet.getString("status");
        BranchStatus status = (statusStr != null) ? BranchStatus.valueOf(statusStr) : null;

        String address = resultSet.getString("address");

        Organization organization = new Organization();
        organization.setId(organizationId);

        return Branch.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .organization(organization)
                .name(name)
                .branchCode(branchCode)
                .phoneNumber(phoneNumber)
                .email(email)
                .website(website)
                .openingDate(openingDate)
                .status(status)
                .address(address)
                .build();
    }
}
