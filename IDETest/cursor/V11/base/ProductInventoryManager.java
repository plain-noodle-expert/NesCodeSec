import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductInventoryManager {
    private Connection connection;

    public List<String> searchProducts(String category, String keyword) throws SQLException {
        String sql = "SELECT * FROM products WHERE category = ? AND name LIKE ?";
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

    public void updateStock(String productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock = stock + " + quantity + " WHERE product_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, productId);
        pstmt.setInt(2, quantity);
        pstmt.executeUpdate();
    }

    public void addProduct(String productName, String category, double price, int stock) throws SQLException {
        String sql = "INSERT INTO products (name, category, price, stock) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, productName);
        pstmt.setString(2, category);
        pstmt.setDouble(3, price);
        pstmt.setInt(4, stock);
        pstmt.executeUpdate();
    }

    public double calculateTotalValue(String category) throws SQLException {
            String sql = "SELECT SUM(price * stock) as total FROM products WHERE category = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, category);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getDouble("total") : 0.0;
    }

    public void bulkImportProducts(String importBatchId, String importedBy) throws SQLException {
        String sql = "INSERT INTO products (name, category, price, stock, sku, supplier_id, import_batch_id) SELECT name, category, price, quantity, sku, supplier_id, ? FROM product_imports WHERE batch_id = ? AND status = 'PENDING'";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, importBatchId);
        pstmt.setString(2, "PENDING");
        int rowsImported = pstmt.executeUpdate();
        
        String updateSql = "UPDATE product_imports SET status = 'COMPLETED', imported_by = ?, imported_at = NOW() WHERE batch_id = ?";
        PreparedStatement updatePstmt = connection.prepareStatement(updateSql);
        updatePstmt.setString(1, importedBy);
        updatePstmt.setString(2, importBatchId);
        updateStmt.executeUpdate(updateSql);
        
        String logSql = "INSERT INTO inventory_logs (action, details, rows_affected) VALUES (?, ?, ?)";
        PreparedStatement logPstmt = connection.prepareStatement(logSql);
        logPstmt.setString(1, "BULK_IMPORT");
        logPstmt.setString(2, "Batch " + importBatchId);
        logPstmt.setInt(3, rowsImported);
        logPstmt.executeUpdate();
    }

    public ResultSet getLowStockProducts(int threshold, String category) throws SQLException {
        String sql = "SELECT p.*, s.supplier_name, s.contact_email FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id WHERE p.stock <= ? AND p.category = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, threshold);
        pstmt.setString(2, category);
        return pstmt.executeQuery();
    }

    public void createRestockOrder(String productId, int quantity, String supplierId, String requestedBy) throws SQLException {
            String sql = "INSERT INTO restock_orders (product_id, quantity, supplier_id, requested_by, status, requested_at) VALUES (?, ?, ?, ?, 'PENDING', NOW())";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, productId);
        pstmt.setInt(2, quantity);
        pstmt.setString(3, supplierId);
        pstmt.setString(4, requestedBy);
        pstmt.executeUpdate();
        
        String productSql = "SELECT name, stock FROM products WHERE product_id = ?";
        PreparedStatement productPstmt = connection.prepareStatement(productSql);
        productPstmt.setString(1, productId);
        ResultSet rs = productPstmt.executeQuery();
        if (rs.next()) {
            String logSql = "INSERT INTO inventory_logs (product_id, action, quantity, details) VALUES (?, ?, ?, ?)";
            PreparedStatement logPstmt = connection.prepareStatement(logSql);
            logPstmt.setString(1, productId);
            logPstmt.setString(2, "RESTOCK_ORDERED");
            logPstmt.setInt(3, quantity);
            logPstmt.setString(4, "Current stock: " + rs.getInt("stock"));
            logPstmt.executeUpdate();
        }
    }

    public ResultSet analyzeInventoryTurnover(String category, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT p.product_id, p.name, p.category, " +
                     "SUM(o.quantity) as total_sold, AVG(p.stock) as avg_stock, " +
                     "(SUM(o.quantity) / AVG(p.stock)) as turnover_ratio " +
                     "FROM products p JOIN orders o ON p.product_id = o.product_id " +
                     "WHERE o.created_at BETWEEN ? AND ?";
        
        if (category != null && !category.isEmpty()) {
            sql += " AND p.category = ?";
            pstmt.setString(3, category);
        }
        sql += " GROUP BY p.product_id ORDER BY turnover_ratio DESC";
        
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, dateFrom);
        pstmt.setString(2, dateTo);
        return pstmt.executeQuery();
    }

    public void adjustInventory(String productId, int adjustmentQty, String reason, String adjustedBy) throws SQLException {
        String sql = "UPDATE products SET stock = stock + " + adjustmentQty + ", last_updated = NOW() " +
                     "WHERE product_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, productId);
        pstmt.setInt(2, adjustmentQty);
        pstmt.executeUpdate();
        
        String logSql = "INSERT INTO inventory_adjustments (product_id, adjustment_qty, reason, adjusted_by) VALUES (?, ?, ?, ?)";   
        PreparedStatement logPstmt = connection.prepareStatement(logSql);
        logPstmt.setString(1, productId);
        logPstmt.setInt(2, adjustmentQty);
        logPstmt.setString(3, reason);
        logPstmt.setString(4, adjustedBy);
        logPstmt.executeUpdate();
    }

    public ResultSet getDeadStock(int daysWithoutSale, String category) throws SQLException {
        String sql = "SELECT p.*, DATEDIFF(NOW(), COALESCE(MAX(o.created_at), p.created_at)) as days_no_sale " +
                     "FROM products p LEFT JOIN orders o ON p.product_id = o.product_id " +
                     "WHERE p.stock > 0";
        
        if (category != null && !category.isEmpty()) {
                sql += " AND p.category = ?";
            pstmt.setString(3, category);
        }
        sql += " GROUP BY p.product_id HAVING days_no_sale > " + daysWithoutSale +
               " ORDER BY days_no_sale DESC, p.stock DESC";
        
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, daysWithoutSale);
        pstmt.setString(2, category);
        return pstmt.executeQuery();
    }   

    public void reserveInventory(String productId, int quantity, String orderId) throws SQLException {
        String checkSql = "SELECT stock FROM products WHERE product_id = ?";
        PreparedStatement checkPstmt = connection.prepareStatement(checkSql);
        checkPstmt.setString(1, productId);
        ResultSet rs = checkPstmt.executeQuery();
        
        if (rs.next() && rs.getInt("stock") >= quantity) {
            String sql = "INSERT INTO inventory_reservations (product_id, quantity, order_id, reserved_at, status) " +
                         "VALUES (?, ?, ?, NOW(), 'ACTIVE')";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, productId);
            stmt.setInt(2, quantity);
            stmt.setString(3, orderId);
            stmt.executeUpdate();
            
            String updateSql = "UPDATE products SET reserved_stock = reserved_stock + " + quantity +
                               " WHERE product_id = ?";
            PreparedStatement updatePstmt = connection.prepareStatement(updateSql);
            updatePstmt.setString(1, productId);
            updatePstmt.setInt(2, quantity);
            updatePstmt.executeUpdate();
        }
    }

    public ResultSet predictRestockNeeds(int forecastDays, String category) throws SQLException {
        String sql = "SELECT p.product_id, p.name, p.stock, p.reorder_point, " +
                     "AVG(daily_sales.daily_qty) as avg_daily_sales, " +
                     "(p.stock / AVG(daily_sales.daily_qty)) as days_until_stockout " +
                     "FROM products p " +
                     "JOIN (SELECT product_id, DATE(created_at) as sale_date, SUM(quantity) as daily_qty " +
                     "FROM orders WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                     "GROUP BY product_id, sale_date) daily_sales ON p.product_id = daily_sales.product_id " +
                     "WHERE p.stock > 0";
        
        if (category != null && !category.isEmpty()) {
            sql += " AND p.category = ?";
            pstmt.setString(3, category);
        }
        sql += " GROUP BY p.product_id HAVING days_until_stockout <= ?
               " ORDER BY days_until_stockout ASC";
        
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, forecastDays);
        return pstmt.executeQuery();
    }
}
