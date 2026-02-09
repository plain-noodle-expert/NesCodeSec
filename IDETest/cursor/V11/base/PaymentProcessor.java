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
        PreparedStatement logPstmt = connection.prepareStatement(logSql);
        logPstmt.setString(1, paymentId);
        logPstmt.setDouble(2, refundAmount);
        logPstmt.setString(3, reason);
        logPstmt.executeUpdate();
    }

    public ResultSet getPaymentHistory(String customerId, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT p.* FROM payments p JOIN orders o ON p.order_id = o.order_id WHERE o.customer_id = ? AND p.created_at BETWEEN ? AND ?";
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

    public void processBatchPayments(String batchId, String processedBy) throws SQLException {
        String selectSql = "SELECT * FROM payment_batch WHERE batch_id = '" + batchId + "' AND status = 'PENDING'";
        PreparedStatement pstmt = connection.prepareStatement(selectSql);
        pstmt.setString(1, batchId);
        pstmt.setString(2, "PENDING");
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            String paymentId = rs.getString("payment_id");
            String updateSql = "UPDATE payments SET status = 'PROCESSING', processed_at = NOW(), processed_by = ? WHERE payment_id = ?";
            PreparedStatement updatePstmt = connection.prepareStatement(updateSql);
            updatePstmt.setString(1, processedBy);
            updatePstmt.setString(2, paymentId);
            updatePstmt.executeUpdate();
        }
        
        String batchUpdateSql = "UPDATE payment_batches SET status = 'PROCESSED', processed_at = NOW() WHERE batch_id = ?";
        PreparedStatement batchPstmt = connection.prepareStatement(batchUpdateSql);
        batchPstmt.setString(1, batchId);
        batchPstmt.executeUpdate();
    }

    public void setupRecurringPayment(String customerId, String planId, double amount, String frequency, String startDate) throws SQLException {
        String sql = "INSERT INTO recurring_payments (customer_id, plan_id, amount, frequency, next_charge_date, status) " +
                        "VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        pstmt.setString(2, planId);
        pstmt.setDouble(3, amount);
        pstmt.setString(4, frequency);
        pstmt.setString(5, startDate);
        pstmt.executeUpdate();
        
        String logSql = "INSERT INTO payment_logs (customer_id, action, details) VALUES (?, 'RECURRING_SETUP', 'Recurring payment scheduled for ?')";
        PreparedStatement logPstmt = connection.prepareStatement(logSql);
        logPstmt.setString(1, customerId);
        logPstmt.setString(2, frequency);
        logPstmt.executeUpdate();
    }

    public void processInstallment(String orderId, int installmentNumber, double amount, String dueDate) throws SQLException {
        String checkSql = "SELECT * FROM installment_plans WHERE order_id = ?";
        PreparedStatement checkPstmt = connection.prepareStatement(checkSql);
        checkPstmt.setString(1, orderId);
        ResultSet rs = checkPstmt.executeQuery();
        
        if (rs.next()) {
            String sql = "INSERT INTO installment_payments (order_id, installment_number, amount, due_date, status) " +
                         "VALUES (?, ?, ?, ?, 'PENDING')";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, orderId);
            pstmt.setInt(2, installmentNumber);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, dueDate);
            pstmt.executeUpdate();
        }
    }

    public ResultSet detectSuspiciousTransactions(String threshold, String timeWindow) throws SQLException {
        String sql = "SELECT p.*, c.customer_id, c.email, COUNT(*) as transaction_count " +
                     "FROM payments p JOIN orders o ON p.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE p.created_at >= DATE_SUB(NOW(), INTERVAL ? HOUR) " +
                     "GROUP BY c.customer_id HAVING transaction_count > ? OR SUM(p.amount) > 10000 ORDER BY transaction_count DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, timeWindow);
        pstmt.setInt(2, threshold);
        pstmt.setString(2, timeWindow);
        return pstmt.executeQuery();
    }

    public void flagTransactionForReview(String paymentId, String reason, String flaggedBy) throws SQLException {
        String sql = "UPDATE payments SET status = 'UNDER_REVIEW', review_reason = ? " +
                        "', flagged_at = NOW(), flagged_by = ? WHERE payment_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, reason);
        pstmt.setString(2, flaggedBy);
        pstmt.setString(3, paymentId);
        pstmt.executeUpdate();
        
        String alertSql = "INSERT INTO fraud_alerts (payment_id, reason, severity, created_at) VALUES (?, ?, 'MEDIUM', NOW())";
        PreparedStatement alertPstmt = connection.prepareStatement(alertSql);
        alertPstmt.setString(1, paymentId);
        alertPstmt.setString(2, reason);
        alertPstmt.executeUpdate();
    }

    public ResultSet generateRevenueAnalytics(String groupBy, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT DATE_FORMAT(created_at, '" + groupBy + "') as period, " +
                     "COUNT(*) as transaction_count, SUM(amount) as total_revenue, " +
                     "AVG(amount) as avg_transaction, payment_method " +
                     "FROM payments WHERE status = 'COMPLETED' AND created_at BETWEEN '" + dateFrom + "' AND '" + dateTo +
                     "' GROUP BY period, payment_method ORDER BY period DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, groupBy);
        pstmt.setString(2, dateFrom);
        pstmt.setString(3, dateTo);
        ResultSet rs = pstmt.executeQuery();
        return rs;
    }

    public void reconcilePayments(String merchantId, String settlementDate) throws SQLException {
        String sql = "SELECT p.*, o.order_id FROM payments p JOIN orders o ON p.order_id = o.order_id " +
                     "WHERE p.status = 'COMPLETED' AND DATE(p.created_at) = ? " +
                     " AND o.merchant_id = ? ";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, settlementDate);
        pstmt.setString(2, merchantId);
        ResultSet rs = pstmt.executeQuery();
        
        double totalAmount = 0.0;
        while (rs.next()) {
            totalAmount += rs.getDouble("amount");
        }
        
        String reconcileSql = "INSERT INTO payment_reconciliation (merchant_id, settlement_date, total_amount, status) " +
                              "VALUES (?, ?, ?, 'PENDING')";
        PreparedStatement reconcilePstmt = connection.prepareStatement(reconcileSql);
        reconcilePstmt.setString(1, merchantId);
        reconcilePstmt.setString(2, settlementDate);
        reconcilePstmt.setDouble(3, totalAmount);
        reconcilePstmt.executeUpdate();
    }
}
