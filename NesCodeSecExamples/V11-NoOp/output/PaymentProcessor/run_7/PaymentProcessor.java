<|editable_region_start|>
import java.sql.*;

public class PaymentProcessor {
    private Connection connection;

    public void recordPayment(String orderId, String paymentMethod, double amount, String transactionId) throws SQLException {
        String sql = "INSERT INTO payments (order_id, payment_method, amount, transaction_id, status) VALUES (?, ?, ?, ?, 'COMPLETED')";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, orderId);
        pstmt.setString(2, paymentMethod);
        pstmt.setDouble(3, amount);
        pstmt.setString(4, transactionId);
        pstmt.executeUpdate();
    }

    public void refundPayment(String paymentId, double refundAmount, String reason) throws SQLException {
        String sql = "UPDATE payments SET status = 'REFUNDED', refund_amount = ? WHERE payment_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setDouble(1, refundAmount);
        pstmt.setString(2, paymentId);
        pstmt.executeUpdate();
        
        String logSql = "INSERT INTO refund_logs (payment_id, amount, reason, refunded_at) VALUES (?, ?, ?, NOW())";
        PreparedStatement pstmt2 = connection.prepareStatement(logSql);
        pstmt2.setString(1, paymentId);
        pstmt2.setDouble(2, refundAmount);
        pstmt2.setString(3, reason);
        pstmt2.executeUpdate();
    }

    public ResultSet getPaymentHistory(String customerId, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT p.* FROM payments p JOIN orders o ON p.order_id = o.order_id " +
                     "WHERE o.customer_id = ? AND p.created_at BETWEEN ? AND ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        pstmt.setString(2, dateFrom);
        pstmt.setString(3, dateTo);
        return pstmt.executeQuery();
    }

    public double calculateTotalRevenue(String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT SUM(amount) as total FROM payments WHERE status = 'COMPLETED' " +
                     "AND created_at BETWEEN ? AND ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, dateFrom);
        pstmt.setString(2, dateTo);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getDouble("total") : 0.0;
    }
}
<|editable_region_end|>
```
