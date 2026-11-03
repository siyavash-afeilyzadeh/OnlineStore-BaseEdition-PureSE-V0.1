package com.black.onlinestorese.products.repository;

import com.black.onlinestorese.products.entity.ProductCategory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductCategoryRepository {

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

    public ProductCategory save(ProductCategory productCategory) {
        productCategory.onCreated();

        String sql = "INSERT INTO product_categories (version, deleted, created_at, updated_at, deleted_at, \"productCategory_title\", \"productCategory_description\", is_active, parent_category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, productCategory.getVersion() != null ? productCategory.getVersion() : 0L);
                    preparedStatement.setBoolean(2, productCategory.isDeleted());
                    preparedStatement.setTimestamp(3, productCategory.getCreatedAt() != null ? Timestamp.valueOf(productCategory.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, productCategory.getUpdatedAt() != null ? Timestamp.valueOf(productCategory.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, productCategory.getDeletedAt() != null ? Timestamp.valueOf(productCategory.getDeletedAt()) : null);
                    preparedStatement.setString(6, productCategory.getTitle());
                    preparedStatement.setString(7, productCategory.getDescription());
                    preparedStatement.setBoolean(8, productCategory.getActive() != null ? productCategory.getActive() : true);

                    if (productCategory.getParentCategory() != null && productCategory.getParentCategory().getId() != null) {
                        preparedStatement.setLong(9, productCategory.getParentCategory().getId());
                    } else {
                        preparedStatement.setNull(9, Types.BIGINT);
                    }

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating product category failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            productCategory.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating product category failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return productCategory;
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

    public Optional<ProductCategory> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, \"productCategory_title\", \"productCategory_description\", is_active, parent_category_id " +
                "FROM product_categories WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToProductCategory(resultSet));
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

    public List<ProductCategory> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, \"productCategory_title\", \"productCategory_description\", is_active, parent_category_id " +
                "FROM product_categories WHERE deleted = false ORDER BY id ASC";

        List<ProductCategory> categories = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            categories.add(mapRowToProductCategory(resultSet));
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

        return categories;
    }

    // ==================== Update ====================

    public void update(ProductCategory productCategory) {
        productCategory.onUpdated();

        String sql = "UPDATE product_categories SET version = ?, updated_at = ?, \"productCategory_title\" = ?, \"productCategory_description\" = ?, is_active = ?, parent_category_id = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, productCategory.getVersion() != null ? productCategory.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(productCategory.getUpdatedAt()));
                    preparedStatement.setString(3, productCategory.getTitle());
                    preparedStatement.setString(4, productCategory.getDescription());
                    preparedStatement.setBoolean(5, productCategory.getActive() != null ? productCategory.getActive() : true);

                    if (productCategory.getParentCategory() != null && productCategory.getParentCategory().getId() != null) {
                        preparedStatement.setLong(6, productCategory.getParentCategory().getId());
                    } else {
                        preparedStatement.setNull(6, Types.BIGINT);
                    }

                    preparedStatement.setLong(7, productCategory.getId());

                    int updated = preparedStatement.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Product category update failed: record not found or marked as deleted.");
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
        ProductCategory productCategory = new ProductCategory();
        productCategory.onDeleted();

        String sql = "UPDATE product_categories SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, Timestamp.valueOf(productCategory.getDeletedAt()));
                    preparedStatement.setLong(2, id);

                    int affected = preparedStatement.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Soft delete failed: product category not found or already deleted.");
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

    // ==================== Find By Title ====================

    public Optional<ProductCategory> findByTitle(String title) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, \"productCategory_title\", \"productCategory_description\", is_active, parent_category_id " +
                "FROM product_categories WHERE \"productCategory_title\" = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, title);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToProductCategory(resultSet));
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
            throw new RuntimeException("Database error in findByTitle(): " + e.getMessage(), e);
        }
    }

    // ==================== Find Sub Categories ====================

    public List<ProductCategory> findSubCategories(Long parentId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, \"productCategory_title\", \"productCategory_description\", is_active, parent_category_id " +
                "FROM product_categories WHERE parent_category_id = ? AND deleted = false ORDER BY id ASC";

        List<ProductCategory> categories = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, parentId);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            categories.add(mapRowToProductCategory(resultSet));
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
            throw new RuntimeException("Database error in findSubCategories(): " + e.getMessage(), e);
        }

        return categories;
    }

    // ==================== Count Sub Categories ====================

    public long countSubCategories(Long parentId) {
        String sql = "SELECT count(id) FROM product_categories WHERE parent_category_id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, parentId);

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
            throw new RuntimeException("Database error in countSubCategories(): " + e.getMessage(), e);
        }

        return 0L;
    }

    // ==================== Row Mappers ====================

    private ProductCategory mapRowToProductCategory(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp deletedTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedTs != null) ? deletedTs.toLocalDateTime() : null;

        String title = resultSet.getString("productCategory_title");
        String description = resultSet.getString("productCategory_description");
        Boolean isActive = resultSet.getBoolean("is_active");
        if (resultSet.wasNull()) {
            isActive = null;
        }

        Long parentId = resultSet.getLong("parent_category_id");
        ProductCategory parentCategoryStub = null;
        if (parentId != 0 && !resultSet.wasNull()) {
            parentCategoryStub = ProductCategory.builder()
                    .id(parentId)
                    .build();
        }

        return ProductCategory.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .title(title)
                .description(description)
                .isActive(isActive)
                .parentCategory(parentCategoryStub)
                .build();
    }
}