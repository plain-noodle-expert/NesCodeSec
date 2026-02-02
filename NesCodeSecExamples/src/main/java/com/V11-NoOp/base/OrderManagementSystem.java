import java.sql.*;

public class OrderManagementSystem {
    private Connection connection;

    public void createOrder(String customerId, String productId, int quantity, String shippingAddress) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, product_id, quantity, shipping_address, status) VALUES ('" +
                     customerId + "', '" + productId + "', " + quantity + ", '" + shippingAddress + "', 'PENDING')";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void updateOrderStatus(String orderId, String newStatus) throws SQLException {
        String sql = "UPDATE orders SET status = '" + newStatus + "' WHERE order_id = '" + orderId + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet getOrdersByCustomer(String customerId, String statusFilter) throws SQLException {
        String sql = "SELECT * FROM orders WHERE customer_id = '" + customerId + "'";
        if (statusFilter != null && !statusFilter.isEmpty()) {
            sql += " AND status = '" + statusFilter + "'";
        }
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void deleteOrder(String orderId, String reason) throws SQLException {
        String logSql = "INSERT INTO order_deletions (order_id, reason, deleted_at) VALUES ('" + 
                        orderId + "', '" + reason + "', NOW())";
        Statement stmt1 = connection.createStatement();
        stmt1.executeUpdate(logSql);
        
        String deleteSql = "DELETE FROM orders WHERE order_id = '" + orderId + "'";
        Statement stmt2 = connection.createStatement();
        stmt2.executeUpdate(deleteSql);
    }
}
