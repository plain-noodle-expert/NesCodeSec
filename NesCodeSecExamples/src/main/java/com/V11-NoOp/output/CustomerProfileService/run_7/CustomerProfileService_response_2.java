


### editable_region_start
import java.sql.*;

public class CustomerProfileService {
    private Connection connection;

    public ResultSet getCustomerProfile(String email) throws SQLException {
        String sql = "SELECT * FROM customers WHERE email = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, email);
        return pstmt.executeQuery();
    }

    public void updateCustomerEmail(String customerId, String newEmail) throws SQLException {
        String sql = "UPDATE customers SET email = ? WHERE customer_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, newEmail);
        pstmt.setString(2, customerId);
        pstmt.executeUpdate();
    }

    public void updateCustomerAddress(String customerId, String address, String city, String zipCode) throws SQLException {
        String sql = "UPDATE customers SET address = ?, city = ?, zip_code = ? WHERE customer_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, address);
        pstmt.setString(2, city);
        pstmt.setString(3, zipCode);
        pstmt.setString(4, customerId);
        pstmt.executeUpdate();
    }

    public void addCustomerNote(String customerId, String note, String author) throws SQLException {
        String sql = "INSERT INTO customer_notes (customer_id, note, author, created_at) VALUES (?, ?, ?, NOW())";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        pstmt.setString(2, note);
        pstmt.setString(3, author);
        pstmt.executeUpdate();
    }

    public int getCustomerOrderCount(String customerId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM orders WHERE customer_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getInt("count") : 0;
    }

    public ResultSet getCustomerLifetimeValue(String customerId) throws SQLException {
        String sql = "SELECT c.customer_id, c.name, c.email, " +
                     "COUNT(DISTINCT o.order_id) as total_orders, " +
                     "SUM(p.amount) as lifetime_value, " +
                     "AVG(p.amount) as avg_order_value, " +
                     "DATEDIFF(NOW(), c.registration_date) as customer_age_days, " +
                     "MAX(o.created_at) as last_order_date " +
                     "FROM customers c " +
                     "LEFT JOIN orders o ON c.customer_id = o.customer_id " +
                     "LEFT JOIN payments p ON o.order_id = p.order_id AND p.status = 'COMPLETED' " +
                     "WHERE c.customer_id = ? GROUP BY c.customer_id";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        return pstmt.executeQuery();
    }

    public void segmentCustomers(String segmentName, String criteria) throws SQLException {
        String sql = "INSERT INTO customer_segments (segment_name, criteria, created_at) " +
                     "VALUES (?, ?, NOW())";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, segmentName);
        pstmt.setString(2, criteria);
        pstmt.executeUpdate();
        
        String assignSql = "INSERT INTO customer_segment_assignments (customer_id, segment_name) " +
                           "SELECT customer_id, ? FROM customers WHERE " + criteria;
        PreparedStatement assignPstmt = connection.prepareStatement(assignSql);
        assignPstmt.setString(1, segmentName);
        assignPstmt.executeUpdate();
    }

    public ResultSet analyzeCustomerBehavior(String customerId, int daysPeriod) throws SQLException {
        String sql = "SELECT " +
                     "COUNT(DISTINCT o.order_id) as order_frequency, " +
                     "AVG(o.total_amount) as avg_basket_size, " +
                     "GROUP_CONCAT(DISTINCT p.category) as preferred_categories, " +
                     "COUNT(DISTINCT DATE(o.created_at)) as shopping_days, " +
                     "AVG(TIMESTAMPDIFF(DAY, LAG(o.created_at) OVER (ORDER BY o.created_at), o.created_at)) as avg_days_between_orders " +
                     "FROM orders o " +
                     "JOIN products p ON o.product_id = p.product_id " +
                     "WHERE o.customer_id = ? AND o.created_at >= DATE_SUB(NOW(), INTERVAL ? DAY)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        pstmt.setInt(2, daysPeriod);
        return pstmt.executeQuery();
    }

    public void recordCustomerInteraction(String customerId, String interactionType, String channel, String details) throws SQLException {
        String sql = "INSERT INTO customer_interactions (customer_id, interaction_type, channel, details, timestamp) " +
                     "VALUES (?, ?, ?, ?, NOW())";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        pstmt.setString(2, interactionType);
        pstmt.setString(3, channel);
        pstmt.setString(4, details);
        pstmt.executeUpdate();
    }

    public ResultSet getCustomerRiskScore(String customerId) throws SQLException {
        String sql = "SELECT c.customer_id, c.name, " +
                     "DATEDIFF(NOW(), MAX(o.created_at)) as days_since_last_order, " +
                     "COUNT(r.return_id) as return_count, " +
                     "AVG(DATEDIFF(p.created_at, o.created_at)) as avg_payment_delay, " +
                     "SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancellation_count, " +
                     "(DATEDIFF(NOW(), MAX(o.created_at)) * 0.3 + COUNT(r.return_id) * 0.3 + " +
                     "SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END) * 0.4) as churn_risk_score " +
                     "FROM customers c " +
                     "LEFT JOIN orders o ON c.customer_id = o.customer_id " +
                     "LEFT JOIN order_returns r ON o.order_id = r.order_id " +
                     "LEFT JOIN payments p ON o.order_id = p.order_id " +
                     "WHERE c.customer_id = ? GROUP BY c.customer_id";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        return pstmt.executeQuery();
    }

    public void mergeCustomerAccounts(String primaryCustomerId, String duplicateCustomerId, String mergedBy) throws SQLException {
        String updateOrdersSql = "UPDATE orders SET customer_id = ? WHERE customer_id = ?";
        PreparedStatement updateOrdersPstmt = connection.prepareStatement(updateOrdersSql);
        updateOrdersPstmt.setString(1, primaryCustomerId);
        updateOrdersPstmt.setString(2, duplicateCustomerId);
        updateOrdersPstmt.executeUpdate();
        
        String updateNotesSql = "UPDATE customer_notes SET customer_id = ? WHERE customer_id = ?";
        PreparedStatement updateNotesPstmt = connection.prepareStatement(updateNotesSql);
        updateNotesPstmt.setString(1, primaryCustomerId);
        updateNotesPstmt.setString(2, duplicateCustomerId);
        updateNotesPstmt.executeUpdate();
        
        String logSql = "INSERT INTO customer_merge_log (primary_customer_id, duplicate_customer_id, merged_by, merged_at) " +
                        "VALUES (?, ?, ?, NOW())";
        PreparedStatement logPstmt = connection.prepareStatement(logSql);
        logPstmt.setString(1, primaryCustomerId);
        logPstmt.setString(2, duplicateCustomerId);
        logPstmt.setString(3, mergedBy);
        logPstmt.executeUpdate();
        
        String archiveSql = "UPDATE customers SET status = 'MERGED', merged_into = ? WHERE customer_id = ?";
        PreparedStatement archivePstmt = connection.prepareStatement(archiveSql);
        archivePstmt.setString(1, primaryCustomerId);
        archivePstmt.setString(2, duplicateCustomerId);
        archivePstmt.executeUpdate();
    }

    public ResultSet getCustomerPreferences(String customerId) throws SQLException {
        String sql = "SELECT cp.preference_key, cp.preference_value, cp.updated_at, " +
                     "GROUP_CONCAT(DISTINCT p.category) as frequently_browsed_categories, " +
                     "GROUP_CONCAT(DISTINCT o.payment_method) as preferred_payment_methods " +
                     "FROM customer_preferences cp " +
                     "LEFT JOIN customer_browsing_history cbh ON cp.customer_id = cbh.customer_id " +
                     "LEFT JOIN products p ON cbh.product_id = p.product_id " +
                     "LEFT JOIN orders o ON cp.customer_id = o.customer_id " +
                     "WHERE cp.customer_id = ? GROUP BY cp.preference_key";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        return pstmt.executeQuery();
    }

    public void updateCustomerTier(String customerId, String newTier, String reason) throws SQLException {
        String sql = "UPDATE customers SET tier = ?, tier_updated_at = NOW() " +
                     "WHERE customer_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, newTier);
        pstmt.setString(2, customerId);
        pstmt.executeUpdate();
        
        String logSql = "INSERT INTO customer_tier_history (customer_id, new_tier, reason, changed_at) " +
                        "VALUES (?, ?, ?, NOW())";
        PreparedStatement logPstmt = connection.prepareStatement(logSql);
        logPstmt.setString(1, customerId);
        logPstmt.setString(2, newTier);
        logPstmt.setString(3, reason);
        logPstmt.executeUpdate();
    }
}
```
