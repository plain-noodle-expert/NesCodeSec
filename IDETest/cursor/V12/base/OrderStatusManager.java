import java.sql.*;

class OrderStatusManager {
    private Connection connection;

    public void searchOrdersByStatus(String status) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        
        String sql = "SELECT * FROM orders WHERE status = '" + status + "'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            System.out.println("Order ID: " + rs.getInt("id"));
        }
    }
    
    public void updateOrderStatus(int orderId, String newStatus) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "UPDATE orders SET status = '" + newStatus + "' WHERE id = " + orderId;
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        System.out.println("Order status updated successfully.");
        stmt.close();
    }

    public void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
}
