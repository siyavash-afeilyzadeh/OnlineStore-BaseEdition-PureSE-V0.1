package com.black.onlinestorese.organization.repository;

import com.black.onlinestorese.organization.entity.Branch;
import com.black.onlinestorese.organization.entity.Department;
import com.black.onlinestorese.organization.entity.enums.DepartmentStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentRepository {

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

    public Department save(Department department) {
        department.onCreated();

        String sql = "INSERT INTO departments (version, deleted, created_at, updated_at, deleted_at, branch_id, name, code, description, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, department.getVersion() != null ? department.getVersion() : 0L);
                    preparedStatement.setBoolean(2, department.isDeleted());
                    preparedStatement.setTimestamp(3, department.getCreatedAt() != null ? Timestamp.valueOf(department.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, department.getUpdatedAt() != null ? Timestamp.valueOf(department.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, department.getDeletedAt() != null ? Timestamp.valueOf(department.getDeletedAt()) : null);
                    preparedStatement.setLong(6, department.getBranch().getId());
                    preparedStatement.setString(7, department.getName());
                    preparedStatement.setString(8, department.getCode());
                    preparedStatement.setString(9, department.getDescription());
                    preparedStatement.setString(10, department.getStatus() != null ? department.getStatus().name() : DepartmentStatus.ACTIVE.name());

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating department failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            department.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating department failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return department;
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

    public Optional<Department> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, branch_id, name, code, description, status " +
                "FROM departments WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToDepartment(resultSet));
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

    public List<Department> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, branch_id, name, code, description, status " +
                "FROM departments WHERE deleted = false ORDER BY id ASC";

        List<Department> departments = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            departments.add(mapRowToDepartment(resultSet));
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

        return departments;
    }

    // ==================== Update ====================

    public void update(Department department) {
        department.onUpdated();

        String sql = "UPDATE departments SET version = ?, updated_at = ?, branch_id = ?, name = ?, code = ?, description = ?, status = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, department.getVersion() != null ? department.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(department.getUpdatedAt()));
                    preparedStatement.setLong(3, department.getBranch().getId());
                    preparedStatement.setString(4, department.getName());
                    preparedStatement.setString(5, department.getCode());
                    preparedStatement.setString(6, department.getDescription());
                    preparedStatement.setString(7, department.getStatus() != null ? department.getStatus().name() : null);
                    preparedStatement.setLong(8, department.getId());

                    int updated = preparedStatement.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Department update failed: record not found or marked as deleted.");
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
        Department department = new Department();
        department.onDeleted();

        String sql = "UPDATE departments SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, Timestamp.valueOf(department.getDeletedAt()));
                    preparedStatement.setLong(2, id);

                    int affected = preparedStatement.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Soft delete failed: department not found or already deleted.");
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

    // ==================== Find By Branch ID ====================

    public List<Department> findByBranchId(Long branchId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, branch_id, name, code, description, status " +
                "FROM departments WHERE branch_id = ? AND deleted = false ORDER BY id ASC";

        List<Department> departments = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, branchId);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            departments.add(mapRowToDepartment(resultSet));
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
            throw new RuntimeException("Database error in findByBranchId(): " + e.getMessage(), e);
        }

        return departments;
    }

    // ==================== Count By Branch ID ====================

    public long countByBranchId(Long branchId) {
        String sql = "SELECT count(id) FROM departments WHERE branch_id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, branchId);

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
            throw new RuntimeException("Database error in countByBranchId(): " + e.getMessage(), e);
        }

        return 0L;
    }

    // ==================== Find By Branch ID And Code ====================

    public Optional<Department> findByBranchIdAndCode(Long branchId, String code) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, branch_id, name, code, description, status " +
                "FROM departments WHERE branch_id = ? AND code = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, branchId);
                    preparedStatement.setString(2, code);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToDepartment(resultSet));
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
            throw new RuntimeException("Database error in findByBranchIdAndCode(): " + e.getMessage(), e);
        }
    }

    // ==================== Find By Employee ID ====================

    public List<Department> findByEmployeeId(Long userId) {
        String sql = "SELECT d.id, d.version, d.deleted, d.created_at, d.updated_at, d.deleted_at, d.branch_id, d.name, d.code, d.description, d.status " +
                "FROM departments d " +
                "INNER JOIN department_employees de ON d.id = de.department_id " +
                "WHERE de.user_id = ? AND d.deleted = false ORDER BY d.id ASC";

        List<Department> departments = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, userId);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            departments.add(mapRowToDepartment(resultSet));
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
            throw new RuntimeException("Database error in findByEmployeeId(): " + e.getMessage(), e);
        }

        return departments;
    }

    // ==================== Row Mappers ====================

    private Department mapRowToDepartment(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp deletedTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedTs != null) ? deletedTs.toLocalDateTime() : null;

        Long branchId = resultSet.getLong("branch_id");
        String name = resultSet.getString("name");
        String code = resultSet.getString("code");
        String description = resultSet.getString("description");

        String statusStr = resultSet.getString("status");
        DepartmentStatus status = (statusStr != null) ? DepartmentStatus.valueOf(statusStr) : null;

        Branch branch = new Branch();
        branch.setId(branchId);

        return Department.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .branch(branch)
                .name(name)
                .code(code)
                .description(description)
                .status(status)
                .build();
    }
}
