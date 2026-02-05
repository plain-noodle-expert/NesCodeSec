<|editable_region_start|>
import java.sql.*;

class OrderStatusManager {
    private Connection connection;

    public void searchOrdersByStatus(String status) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        
        String sql = "SELECT * FROM orders WHERE status = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                System.out.println("Order ID: " + rs.getInt("id"));
            }
        }
    }
    
    public void updateOrderStatus(int orderId, String newStatus) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        }
    }
}
<|editable_region_end|>
```
