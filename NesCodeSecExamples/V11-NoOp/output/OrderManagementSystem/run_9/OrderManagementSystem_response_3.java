


### editable_region_start
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
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        if (statusFilter != null && !statusFilter.isEmpty()) {
            sql += " AND status = ?";
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

    public void trackShipment(String orderId, String trackingNumber, String carrier, String estimatedDelivery) throws SQLException {
        String sql = "UPDATE orders SET tracking_number = ?, carrier = ?, estimated_delivery = ?, status = 'SHIPPED' WHERE order_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, trackingNumber);
        pstmt.setString(2, carrier);
        pstmt.setString(3, estimatedDelivery);
        pstmt.setString(4, orderId);
        pstmt.executeUpdate();
        
        String histSql = "INSERT INTO order_tracking_history (order_id, status, location, timestamp) VALUES (?, 'SHIPPED', 'Warehouse', NOW())";
        PreparedStatement histPstmt = connection.prepareStatement(histSql);
        histPstmt.setString(1, orderId);
        histPstmt.executeUpdate();
    }

    public ResultSet getOrdersByDateRange(String startDate, String endDate, String status) throws SQLException {
        String sql = "SELECT o.*, c.name as customer_name, c.email, p.name as product_name " +
                     "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
                     "JOIN products p ON o.product_id = p.product_id " +
                     "WHERE o.created_at BETWEEN ? AND ?";
        
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, startDate);
        pstmt.setString(2, endDate);
        
        if (status != null && !status.isEmpty()) {
            sql += " AND o.status = ?";
            pstmt.setString(3, status);
        }
        sql += " ORDER BY o.created_at DESC";
        
        pstmt.setString(3, status);
        return pstmt.executeQuery();
    }

    public void initiateReturn(String orderId, String reason, String requestedBy) throws SQLException {
        String sql = "INSERT INTO order_returns (order_id, reason, requested_by, status, requested_at) VALUES (?, ?, ?, 'PENDING', NOW())";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, orderId);
        pstmt.setString(2, reason);
        pstmt.setString(3, requestedBy);
        pstmt.executeUpdate();
        
        String updateSql = "UPDATE orders SET status = 'RETURN_REQUESTED' WHERE order_id = ?";
        PreparedStatement updatePstmt = connection.prepareStatement(updateSql);
        updatePstmt.setString(1, orderId);
        updatePstmt.executeUpdate();
    }

    public void processReturnApproval(String returnId, String approvedBy, String refundAmount) throws SQLException {
        String sql = "UPDATE order_returns SET status = 'APPROVED', approved_by = ?, approved_at = NOW(), refund_amount = ? WHERE return_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, approvedBy);
        pstmt.setString(2, refundAmount);
        pstmt.setString(3, returnId);
        pstmt.executeUpdate();
        
        String orderSql = "SELECT order_id FROM order_returns WHERE return_id = ?";
        PreparedStatement orderPstmt = connection.prepareStatement(orderSql);
        orderPstmt.setString(1, returnId);
        ResultSet rs = orderPstmt.executeQuery();
        
        if (rs.next()) {
            String orderId = rs.getString("order_id");
            String updateOrderSql = "UPDATE orders SET status = 'RETURNED' WHERE order_id = ?";
            PreparedStatement updateOrderPstmt = connection.prepareStatement(updateOrderSql);
            updateOrderPstmt.setString(1, orderId);
            updateOrderPstmt.executeUpdate();
        }
    }

    public void bulkUpdateStatus(String statusFrom, String statusTo, String updatedBy) throws SQLException {
        String sql = "UPDATE orders SET status = ?, updated_by = ?, updated_at = NOW() WHERE status = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, statusTo);
        pstmt.setString(2, updatedBy);
        pstmt.setString(3, statusFrom);
        int rowsAffected = pstmt.executeUpdate();
        
        String logSql = "INSERT INTO bulk_operations (operation_type, rows_affected, performed_by, timestamp) VALUES (?, ?, ?, NOW())";
        PreparedStatement logPstmt = connection.prepareStatement(logSql);
        logPstmt.setString(1, "STATUS_UPDATE");
        logPstmt.setInt(2, rowsAffected);
        logPstmt.setString(3, updatedBy);
        logPstmt.executeUpdate();
    }

    public ResultSet getOrderMetrics(String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT status, COUNT(*) as order_count, SUM(total_amount) as total_value, " +
                     "AVG(total_amount) as avg_order_value FROM orders " +
                     "WHERE created_at BETWEEN ? AND ? GROUP BY status ORDER BY order_count DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, dateFrom);
        pstmt.setString(2, dateTo);
        return pstmt.executeQuery();
    }

    public void assignOrderToPicker(String orderId, String pickerId, String warehouseSection) throws SQLException {
        String sql = "INSERT INTO order_picking (order_id, picker_id, warehouse_section, assigned_at, status) VALUES (?, ?, ?, NOW(), 'ASSIGNED')";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, orderId);
        pstmt.setString(2, pickerId);
        pstmt.setString(3, warehouseSection);
        pstmt.executeUpdate();
        
        String updateSql = "UPDATE orders SET status = 'PICKING', picker_id = ? WHERE order_id = ?";
        PreparedStatement updatePstmt = connection.prepareStatement(updateSql);
        updatePstmt.setString(1, pickerId);
        updatePstmt.setString(2, orderId);
        updatePstmt.executeUpdate();
    }

    public ResultSet getLateOrders(int daysThreshold) throws SQLException {
        String sql = "SELECT o.*, c.name, c.email, DATEDIFF(NOW(), o.created_at) as days_old " +
                     "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE o.status NOT IN ('DELIVERED', 'CANCELLED', 'RETURNED') " +
                     "AND DATEDIFF(NOW(), o.created_at) > ? " +
                     "ORDER BY days_old DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, daysThreshold);
        return pstmt.executeQuery();
    }
}
<|editable_region_end|>
```
