<|editable_region_start|>
import java.sql.*;

public class ReportGenerator {
    private Connection connection;

    public ResultSet generateSalesReport(String startDate, String endDate, String category) throws SQLException {
        String sql = "SELECT p.name, SUM(o.quantity) as total_sold, SUM(o.quantity * p.price) as revenue " +
                     "FROM orders o JOIN products p ON o.product_id = p.product_id " +
                     "WHERE o.created_at BETWEEN ? AND ?";
        
        if (category != null && !category.isEmpty()) {
            sql += " AND p.category = ?";
        }
        sql += " GROUP BY p.name ORDER BY revenue DESC";
        
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, startDate);
        pstmt.setString(2, endDate);
        if (category != null && !category.isEmpty()) {
            pstmt.setString(3, category);
        }
        return pstmt.executeQuery();
    }

    public ResultSet getTopCustomers(int limit, String sortBy) throws SQLException {
        String sql = "SELECT c.customer_id, c.name, c.email, COUNT(o.order_id) as order_count, " +
                     "SUM(p.amount) as total_spent FROM customers c " +
                     "JOIN orders o ON c.customer_id = o.customer_id " +
                     "JOIN payments p ON o.order_id = p.order_id " +
                     "GROUP BY c.customer_id ORDER BY " + sortBy + " DESC LIMIT ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, limit);
        return pstmt.executeQuery();
    }

    public void saveReportSnapshot(String reportType, String parameters, String generatedBy) throws SQLException {
        String sql = "INSERT INTO report_history (report_type, parameters, generated_by, generated_at) VALUES ('" +
                     reportType + "', '" + parameters + "', '" + generatedBy + "', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet getInventoryAlert(String threshold) throws SQLException {
        String sql = "SELECT * FROM products WHERE stock < " + threshold + " ORDER BY stock ASC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
}
<|editable_region_end|>
```
