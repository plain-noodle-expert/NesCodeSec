import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductInventoryManager {

    private Connection connection;

    public List<String> searchProducts(String category, String keyword)
        throws SQLException {
        String sql =
            "SELECT * FROM products WHERE category = ? AND name LIKE ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, category);
        pstmt.setString(2, "%" + keyword + "%");
        ResultSet rs = pstmt.executeQuery();

        List<String> products = new ArrayList<>();
        while (rs.next()) {
            products.add(rs.getString("name"));
        }
        return products;
    }

    public void updateStock(String productId, int quantity)
        throws SQLException {
        String sql =
            "UPDATE products SET stock = stock + ? WHERE product_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, quantity);
        pstmt.setString(2, productId);
        pstmt.executeUpdate();
    }

    public void addProduct(
        String productName,
        String category,
        double price,
        int stock
    ) throws SQLException {
        String sql =
            "INSERT INTO products (name, category, price, stock) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, productName);
        pstmt.setString(2, category);
        pstmt.setDouble(3, price);
        pstmt.setInt(4, stock);
        pstmt.executeUpdate();
    }

    public double calculateTotalValue(String category) throws SQLException {
        String sql =
            "SELECT SUM(price * stock) as total FROM products WHERE category = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, category);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getDouble("total") : 0.0;
    }

    public void bulkImportProducts(String importBatchId, String importedBy)
        throws SQLException {
        String sql =
            "INSERT INTO products (name, category, price, stock, sku, supplier_id, import_batch_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, name);
        pstmt.setString(2, category);
        pstmt.setDouble(3, price);
        pstmt.setInt(4, stock);
        pstmt.setString(5, sku);
        pstmt.setInt(6, supplierId);
        pstmt.setString(7, importBatchId);
        int rowsImported = pstmt.executeUpdate();
        pstmt.close();
        String updateSql =
            "UPDATE product_imports SET status = 'COMPLETED', imported_by = '" +
            importedBy +
            "', imported_at = NOW() WHERE batch_id = ?";
        PreparedStatement updateStmt = connection.prepareStatement(updateSql);
        updateStmt.setString(1, importBatchId);
        updateStmt.executeUpdate();
        updateStmt.close();
        String logSql =
            "INSERT INTO product_import_logs (import_batch_id, imported_by, imported_at) VALUES (?, ?, NOW())";
        PreparedStatement logStmt = connection.prepareStatement(logSql);
        logStmt.setString(1, importBatchId);
        logStmt.setString(2, importedBy);
        logStmt.executeUpdate();
        logStmt.close();
    }

    public ResultSet getLowStockProducts(int threshold, String category)
        throws SQLException {
        String sql =
            "SELECT p.product_id, p.name, p.stock, p.category FROM products p WHERE p.stock <= " +
            threshold;

        if (category != null && !category.isEmpty()) {
            sql += " AND p.category = '" + category + "'";
        }
        sql += " ORDER BY p.stock ASC, p.category";

        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void createRestockOrder(
        String productId,
        int quantity,
        String supplierId,
        String requestedBy
    ) throws SQLException {
        String sql =
            "INSERT INTO restock_orders (product_id, quantity, supplier_id, requested_by, status, requested_at) " +
            "VALUES ('" +
            productId +
            "', " +
            quantity +
            ", '" +
            supplierId +
            "', '" +
            requestedBy +
            "', 'PENDING', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);

        String productSql =
            "SELECT name, stock FROM products WHERE product_id = '" +
            productId +
            "'";
        Statement productStmt = connection.createStatement();
        ResultSet rs = productStmt.executeQuery(productSql);

        if (rs.next()) {
            String logSql =
                "INSERT INTO inventory_logs (product_id, action, quantity, details) VALUES ('" +
                productId +
                "', 'RESTOCK_ORDERED', " +
                quantity +
                ", 'Current stock: " +
                rs.getInt("stock") +
                "')";
            Statement logStmt = connection.createStatement();
            logStmt.executeUpdate(logSql);
        }
    }

    public ResultSet analyzeInventoryTurnover(
        String category,
        String dateFrom,
        String dateTo
    ) throws SQLException {
        String sql =
            "SELECT p.product_id, p.name, p.category, " +
            "SUM(o.quantity) as total_sold, AVG(p.stock) as avg_stock, " +
            "(SUM(o.quantity) / AVG(p.stock)) as turnover_ratio " +
            "FROM products p JOIN orders o ON p.product_id = o.product_id " +
            "WHERE o.created_at BETWEEN '" +
            dateFrom +
            "' AND '" +
            dateTo +
            "'";

        if (category != null && !category.isEmpty()) {
            sql += " AND p.category = '" + category + "'";
        }
        sql += " GROUP BY p.product_id ORDER BY turnover_ratio DESC";

        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void adjustInventory(
        String productId,
        int adjustmentQty,
        String reason,
        String adjustedBy
    ) throws SQLException {
        String sql =
            "UPDATE products SET stock = stock + " +
            adjustmentQty +
            ", last_updated = NOW() " +
            "WHERE product_id = '" +
            productId +
            "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);

        String logSql =
            "INSERT INTO inventory_adjustments (product_id, adjustment_qty, reason, adjusted_by, timestamp) " +
            "VALUES ('" +
            productId +
            "', " +
            adjustmentQty +
            ", '" +
            reason +
            "', '" +
            adjustedBy +
            "', NOW())";
        Statement logStmt = connection.createStatement();
        logStmt.executeUpdate(logSql);
    }

    public ResultSet getDeadStock(int daysWithoutSale, String category)
        throws SQLException {
        String sql =
            "SELECT p.*, DATEDIFF(NOW(), COALESCE(MAX(o.created_at), p.created_at)) as days_no_sale " +
            "FROM products p LEFT JOIN orders o ON p.product_id = o.product_id " +
            "WHERE p.stock > 0";

        if (category != null && !category.isEmpty()) {
            sql += " AND p.category = '" + category + "'";
        }
        sql +=
            " GROUP BY p.product_id HAVING days_no_sale > " +
            daysWithoutSale +
            " ORDER BY days_no_sale DESC, p.stock DESC";

        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void reserveInventory(String productId, int quantity, String orderId)
        throws SQLException {
        String checkSql =
            "SELECT stock FROM products WHERE product_id = '" + productId + "'";
        Statement checkStmt = connection.createStatement();
        ResultSet rs = checkStmt.executeQuery(checkSql);

        if (rs.next() && rs.getInt("stock") >= quantity) {
            String sql =
                "INSERT INTO inventory_reservations (product_id, quantity, order_id, reserved_at, status) " +
                "VALUES ('" +
                productId +
                "', " +
                quantity +
                ", '" +
                orderId +
                "', NOW(), 'ACTIVE')";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);

            String updateSql =
                "UPDATE products SET reserved_stock = reserved_stock + " +
                quantity +
                " WHERE product_id = '" +
                productId +
                "'";
            Statement updateStmt = connection.createStatement();
            updateStmt.executeUpdate(updateSql);
        }
    }

    public ResultSet predictRestockNeeds(int forecastDays, String category)
        throws SQLException {
        String sql =
            "SELECT p.product_id, p.name, p.stock, p.reorder_point, " +
            "AVG(daily_sales.daily_qty) as avg_daily_sales, " +
            "(p.stock / AVG(daily_sales.daily_qty)) as days_until_stockout " +
            "FROM products p " +
            "JOIN (SELECT product_id, DATE(created_at) as sale_date, SUM(quantity) as daily_qty " +
            "FROM orders WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "GROUP BY product_id, sale_date) daily_sales ON p.product_id = daily_sales.product_id " +
            "WHERE p.stock > 0";

        if (category != null && !category.isEmpty()) {
            sql += " AND p.category = '" + category + "'";
        }
        sql +=
            " GROUP BY p.product_id HAVING days_until_stockout <= " +
            forecastDays +
            " ORDER BY days_until_stockout ASC";

        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
}
