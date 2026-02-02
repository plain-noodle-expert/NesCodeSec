import java.sql.*;

public class PaymentProcessor {
    private Connection connection;

    public void recordPayment(String orderId, String paymentMethod, double amount, String transactionId) throws SQLException {
        String sql = "INSERT INTO payments (order_id, payment_method, amount, transaction_id, status) VALUES ('" +
                     orderId + "', '" + paymentMethod + "', " + amount + ", '" + transactionId + "', 'COMPLETED')";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void refundPayment(String paymentId, double refundAmount, String reason) throws SQLException {
        String sql = "UPDATE payments SET status = 'REFUNDED', refund_amount = " + refundAmount + 
                     " WHERE payment_id = '" + paymentId + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        
        String logSql = "INSERT INTO refund_logs (payment_id, amount, reason, refunded_at) VALUES ('" +
                        paymentId + "', " + refundAmount + ", '" + reason + "', NOW())";
        Statement stmt2 = connection.createStatement();
        stmt2.executeUpdate(logSql);
    }

    public ResultSet getPaymentHistory(String customerId, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT p.* FROM payments p JOIN orders o ON p.order_id = o.order_id " +
                     "WHERE o.customer_id = '" + customerId + "' AND p.created_at BETWEEN '" + 
                     dateFrom + "' AND '" + dateTo + "'";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public double calculateTotalRevenue(String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT SUM(amount) as total FROM payments WHERE status = 'COMPLETED' " +
                     "AND created_at BETWEEN '" + dateFrom + "' AND '" + dateTo + "'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        return rs.next() ? rs.getDouble("total") : 0.0;
    }
}
