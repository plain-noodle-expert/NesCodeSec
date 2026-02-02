


### editable_region_start
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
                     "GROUP BY c.customer_id ORDER BY " + sortBy + " DESC LIMIT " + limit;
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
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

    public void scheduleRecurringReport(String reportName, String reportType, String frequency, String recipients, String parameters) throws SQLException {
        String sql = "INSERT INTO scheduled_reports (report_name, report_type, frequency, recipients, parameters, status, created_at) " +
                     "VALUES ('" + reportName + "', '" + reportType + "', '" + frequency + "', '" +
                     recipients + "', '" + parameters + "', 'ACTIVE', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet generateCustomerRetentionReport(String startDate, String endDate) throws SQLException {
        String sql = "SELECT " +
                     "DATE_FORMAT(c.registration_date, '%Y-%m') as cohort_month, " +
                     "COUNT(DISTINCT c.customer_id) as total_customers, " +
                     "COUNT(DISTINCT CASE WHEN o.created_at >= DATE_ADD(c.registration_date, INTERVAL 30 DAY) THEN c.customer_id END) as retained_30_days, " +
                     "COUNT(DISTINCT CASE WHEN o.created_at >= DATE_ADD(c.registration_date, INTERVAL 90 DAY) THEN c.customer_id END) as retained_90_days, " +
                     "(COUNT(DISTINCT CASE WHEN o.created_at >= DATE_ADD(c.registration_date, INTERVAL 30 DAY) THEN c.customer_id END) / COUNT(DISTINCT c.customer_id) * 100) as retention_rate_30d " +
                     "FROM customers c " +
                     "LEFT JOIN orders o ON c.customer_id = o.customer_id " +
                     "WHERE c.registration_date BETWEEN '" + startDate + "' AND '" + endDate +
                     "' GROUP BY cohort_month ORDER BY cohort_month DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public ResultSet generateProductPerformanceReport(String category, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT p.product_id, p.name, p.category, p.price, " +
                     "COUNT(o.order_id) as times_ordered, " +
                     "SUM(o.quantity) as total_units_sold, " +
                     "SUM(o.quantity * p.price) as gross_revenue, " +
                     "AVG(r.rating) as avg_rating, " +
                     "COUNT(r.review_id) as review_count, " +
                     "(SUM(o.quantity * p.price) / SUM(o.quantity)) as avg_selling_price " +
                     "FROM products p " +
                     "LEFT JOIN orders o ON p.product_id = o.product_id AND o.created_at BETWEEN '" + dateFrom + "' AND '" + dateTo + "' " +
                     "LEFT JOIN product_reviews r ON p.product_id = r.product_id " +
                     "WHERE p.category = '" + category +
                     "' GROUP BY p.product_id ORDER BY gross_revenue DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void exportReportData(String reportId, String format, String exportPath, String exportedBy) throws SQLException {
        String sql = "INSERT INTO report_exports (report_id, format, export_path, exported_by, status, exported_at) " +
                     "VALUES ('" + reportId + "', '" + format + "', '" + exportPath + "', '" + exportedBy + "', 'COMPLETED', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet generateABTestReport(String testId, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT " +
                     "abt.variant_name, " +
                     "COUNT(DISTINCT abta.customer_id) as unique_users, " +
                     "COUNT(DISTINCT o.order_id) as total_conversions, " +
                     "SUM(p.amount) as total_revenue, " +
                     "(COUNT(DISTINCT o.order_id) / COUNT(DISTINCT abta.customer_id) * 100) as conversion_rate, " +
                     "AVG(p.amount) as avg_order_value " +
                     "FROM ab_test_assignments abta " +
                     "JOIN ab_tests abt ON abta.test_id = abt.test_id " +
                     "LEFT JOIN orders o ON abta.customer_id = o.customer_id AND o.created_at BETWEEN '" + dateFrom + "' AND '" + dateTo + "' " +
                     "LEFT JOIN payments p ON o.order_id = p.order_id " +
                     "WHERE abt.test_id = '" + testId +
                     "' GROUP BY abt.variant_name ORDER BY conversion_rate DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public ResultSet generateOperationalMetrics(String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT " +
                     "DATE(created_at) as report_date, " +
                     "COUNT(DISTINCT order_id) as total_orders, " +
                     "AVG(TIMESTAMPDIFF(HOUR, created_at, shipped_at)) as avg_fulfillment_time, " +
                     "SUM(CASE WHEN status = 'DELIVERED' THEN 1 ELSE 0 END) as delivered_orders, " +
                     "SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled_orders, " +
                     "(SUM(CASE WHEN status = 'DELIVERED' THEN 1 ELSE 0 END) / COUNT(*) * 100) as success_rate " +
                     "FROM orders " +
                     "WHERE created_at BETWEEN '" + dateFrom + "' AND '" + dateTo +
                     "' GROUP BY report_date ORDER BY report_date DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void cachReportResults(String reportId, String reportData, int ttlMinutes) throws SQLException {
        String sql = "INSERT INTO report_cache (report_id, report_data, expires_at, cached_at) " +
                     "VALUES ('" + reportId + "', '" + reportData + "', " +
                     "DATE_ADD(NOW(), INTERVAL " + ttlMinutes + " MINUTE), NOW()) " +
                     "ON DUPLICATE KEY UPDATE report_data = '" + reportData +
                     "', expires_at = DATE_ADD(NOW(), INTERVAL " + ttlMinutes + " MINUTE), cached_at = NOW()";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet getReportAccessLog(String reportType, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT u.username, ral.report_type, ral.access_time, ral.ip_address, ral.parameters " +
                     "FROM report_access_log ral " +
                     "JOIN users u ON ral.user_id = u.user_id " +
                     "WHERE ral.report_type = '" + reportType +
                     "' AND ral.access_time BETWEEN '" + dateFrom + "' AND '" + dateTo +
                     "' ORDER BY ral.access_time DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
}
```
