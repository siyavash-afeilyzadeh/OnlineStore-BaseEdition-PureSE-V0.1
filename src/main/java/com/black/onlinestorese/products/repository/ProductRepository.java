package com.black.onlinestorese.products.repository;

import com.black.onlinestorese.products.entity.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

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

    public Product save(Product product) {
        product.onCreated();

        String sql = "INSERT INTO products (version, deleted, created_at, updated_at, deleted_at, product_name, product_price, product_description, is_active, product_category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    preparedStatement.setLong(1, product.getVersion() != null ? product.getVersion() : 0L);
                    preparedStatement.setBoolean(2, product.isDeleted());
                    preparedStatement.setTimestamp(3, product.getCreatedAt() != null ? Timestamp.valueOf(product.getCreatedAt()) : null);
                    preparedStatement.setTimestamp(4, product.getUpdatedAt() != null ? Timestamp.valueOf(product.getUpdatedAt()) : null);
                    preparedStatement.setTimestamp(5, product.getDeletedAt() != null ? Timestamp.valueOf(product.getDeletedAt()) : null);
                    preparedStatement.setString(6, product.getName());
                    preparedStatement.setBigDecimal(7, product.getPrice());
                    preparedStatement.setString(8, product.getDescription());
                    preparedStatement.setBoolean(9, product.getActive() != null ? product.getActive() : true);

                    if (product.getProductCategory() != null && product.getProductCategory().getId() != null) {
                        preparedStatement.setLong(10, product.getProductCategory().getId());
                    } else {
                        preparedStatement.setNull(10, Types.BIGINT);
                    }

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating product failed, no rows affected.");
                    }

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    try {
                        if (generatedKeys.next()) {
                            product.setId(generatedKeys.getLong(1));
                        } else {
                            throw new SQLException("Creating product failed, no ID obtained.");
                        }
                    } finally {
                        generatedKeys.close();
                    }

                    return product;
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

    public Optional<Product> findById(Long id) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, product_name, product_price, product_description, is_active, product_category_id " +
                "FROM products WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, id);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToProduct(resultSet));
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

    public List<Product> findAll() {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, product_name, product_price, product_description, is_active, product_category_id " +
                "FROM products WHERE deleted = false ORDER BY id ASC";

        List<Product> products = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            products.add(mapRowToProduct(resultSet));
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

        return products;
    }

    // ==================== Update ====================

    public void update(Product product) {
        product.onUpdated();

        String sql = "UPDATE products SET version = ?, updated_at = ?, product_name = ?, product_price = ?, product_description = ?, is_active = ?, product_category_id = ? " +
                "WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, product.getVersion() != null ? product.getVersion() + 1 : 1L);
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(product.getUpdatedAt()));
                    preparedStatement.setString(3, product.getName());
                    preparedStatement.setBigDecimal(4, product.getPrice());
                    preparedStatement.setString(5, product.getDescription());
                    preparedStatement.setBoolean(6, product.getActive() != null ? product.getActive() : true);

                    if (product.getProductCategory() != null && product.getProductCategory().getId() != null) {
                        preparedStatement.setLong(7, product.getProductCategory().getId());
                    } else {
                        preparedStatement.setNull(7, Types.BIGINT);
                    }

                    preparedStatement.setLong(8, product.getId());

                    int updated = preparedStatement.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Product update failed: record not found or marked as deleted.");
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
        Product product = new Product();
        product.onDeleted();

        String sql = "UPDATE products SET deleted = true, deleted_at = ? WHERE id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setTimestamp(1, Timestamp.valueOf(product.getDeletedAt()));
                    preparedStatement.setLong(2, id);

                    int affected = preparedStatement.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Soft delete failed: product not found or already deleted.");
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

    // ==================== Find By Name ====================

    public Optional<Product> findByName(String name) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, product_name, product_price, product_description, is_active, product_category_id " +
                "FROM products WHERE product_name = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setString(1, name);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToProduct(resultSet));
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
            throw new RuntimeException("Database error in findByName(): " + e.getMessage(), e);
        }
    }

    // ==================== Find By Category ====================

    public List<Product> findByCategory(Long categoryId) {
        String sql = "SELECT id, version, deleted, created_at, updated_at, deleted_at, product_name, product_price, product_description, is_active, product_category_id " +
                "FROM products WHERE product_category_id = ? AND deleted = false ORDER BY id ASC";

        List<Product> products = new ArrayList<>();

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, categoryId);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            products.add(mapRowToProduct(resultSet));
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
            throw new RuntimeException("Database error in findByCategory(): " + e.getMessage(), e);
        }

        return products;
    }

    // ==================== Count By Category ====================

    public long countByCategory(Long categoryId) {
        String sql = "SELECT count(id) FROM products WHERE product_category_id = ? AND deleted = false";

        try {
            Connection connection = getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                try {
                    preparedStatement.setLong(1, categoryId);

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
            throw new RuntimeException("Database error in countByCategory(): " + e.getMessage(), e);
        }

        return 0L;
    }

    // ==================== Row Mappers ====================

    private Product mapRowToProduct(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long version = resultSet.getLong("version");
        boolean deleted = resultSet.getBoolean("deleted");

        Timestamp createdTs = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = resultSet.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp deletedTs = resultSet.getTimestamp("deleted_at");
        LocalDateTime deletedAt = (deletedTs != null) ? deletedTs.toLocalDateTime() : null;

        String name = resultSet.getString("product_name");
        BigDecimal price = resultSet.getBigDecimal("product_price");
        String description = resultSet.getString("product_description");
        Boolean isActive = resultSet.getBoolean("is_active");
        if (resultSet.wasNull()) {
            isActive = null;
        }

        Long categoryId = resultSet.getLong("product_category_id");
        com.black.onlinestorese.products.entity.ProductCategory categoryStub = null;
        if (categoryId != 0 && !resultSet.wasNull()) {
            categoryStub = com.black.onlinestorese.products.entity.ProductCategory.builder()
                    .id(categoryId)
                    .build();
        }

        return Product.builder()
                .id(id)
                .version(version)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .name(name)
                .price(price)
                .description(description)
                .isActive(isActive)
                .productCategory(categoryStub)
                .build();
    }
}
