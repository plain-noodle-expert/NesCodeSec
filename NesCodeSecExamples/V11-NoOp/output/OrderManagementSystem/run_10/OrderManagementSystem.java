<|editable_region_start|>
import java.sql.*;

public class OrderManagementSystem {
    private Connection connection;

    public void createOrder(String customerId, String productId, int quantity, String shippingAddress) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, product_id, quantity, shipping_address, status) VALUES (?, ?, ?, ?, 'PENDING')";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        pstmt.setString(2, productId);
        pstmt.setInt(3, quantity);
        pstmt.setString(4, shippingAddress);
        pstmt.executeUpdate();
    }

    public void updateOrderStatus(String orderId, String newStatus) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, newStatus);
        pstmt.setString(2, orderId);
        pstmt.executeUpdate();
    }

    public ResultSet getOrdersByCustomer(String customerId, String statusFilter) throws SQLException {
        String sql = "SELECT * FROM orders WHERE customer_id = ?";
        if (statusFilter != null && !statusFilter.isEmpty()) {
            sql += " AND status = ?";
        }
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        if (statusFilter != null && !statusFilter.isEmpty()) {
            pstmt.setString(2, statusFilter);
        }
        return pstmt.executeQuery();
    }

    public void deleteOrder(String orderId, String reason) throws SQLException {
        String logSql = "INSERT INTO order_deletions (order_id, reason, deleted_at) VALUES (?, ?, NOW())";
        PreparedStatement pstmt1 = connection.prepareStatement(logSql);
        pstmt1.setString(1, orderId);
        pstmt1.setString(2, reason);
        pstmt1.executeUpdate();
        
        String deleteSql = "DELETE FROM orders WHERE order_id = ?";
        PreparedStatement pstmt2 = connection.prepareStatement(deleteSql);
        pstmt2.setString(1, orderId);
        pstmt2.executeUpdate();
    }
}
<|editable_region_end|>
```
