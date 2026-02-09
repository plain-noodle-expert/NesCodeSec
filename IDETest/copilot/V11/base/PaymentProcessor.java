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

    public void processBatchPayments(String batchId, String processedBy) throws SQLException {
        String selectSql = "SELECT * FROM payment_batch WHERE batch_id = ? AND status = 'PENDING'";
        PreparedStatement selectStmt = connection.prepareStatement(selectSql);
        selectStmt.setString(1, batchId);
        ResultSet rs = selectStmt.executeQuery();
        
        while (rs.next()) {
            String paymentId = rs.getString("payment_id");
            String updateSql = "UPDATE payments SET status = 'PROCESSING', processed_at = NOW(), processed_by = ? WHERE payment_id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateSql);
            updateStmt.setString(1, processedBy);
            updateStmt.setString(2, paymentId);
            updateStmt.executeUpdate();
        }
        
        String batchUpdateSql = "UPDATE payment_batches SET status = 'PROCESSED', processed_at = NOW() WHERE batch_id = ?";
        PreparedStatement batchStmt = connection.prepareStatement(batchUpdateSql);
        batchStmt.setString(1, batchId);
        batchStmt.executeUpdate();
    }

    public void setupRecurringPayment(String customerId, String planId, double amount, String frequency, String startDate) throws SQLException {
        String sql = "INSERT INTO recurring_payments (customer_id, plan_id, amount, frequency, next_charge_date, status) " +
                     "VALUES ('" + customerId + "', '" + planId + "', " + amount + ", '" + frequency +
                     "', '" + startDate + "', 'ACTIVE')";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.executeUpdate();
        
        String logSql = "INSERT INTO payment_logs (customer_id, action, details) VALUES ('" +
                        customerId + "', 'RECURRING_SETUP', 'Recurring payment scheduled for " + frequency + "')";
        PreparedStatement logStmt = connection.prepareStatement(logSql);
        logStmt.executeUpdate();
    }

    public void processInstallment(String orderId, int installmentNumber, double amount, String dueDate) throws SQLException {
        String checkSql = "SELECT * FROM installment_plans WHERE order_id = ? AND status = 'ACTIVE'";
        PreparedStatement checkStmt = connection.prepareStatement(checkSql);
        checkStmt.setString(1, orderId);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next()) {
            String sql = "INSERT INTO installment_payments (order_id, installment_number, amount, due_date, status) " +
                         "VALUES (?, ?, ?, ?, 'PENDING')";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, orderId);
            stmt.setInt(2, installmentNumber);
            stmt.setDouble(3, amount);
            stmt.setString(4, dueDate);
            stmt.executeUpdate();
        }
    }

    public ResultSet detectSuspiciousTransactions(String threshold, String timeWindow) throws SQLException {
        String sql = "SELECT p.*, c.customer_id, c.email, COUNT(*) as transaction_count " +
                     "FROM payments p JOIN orders o ON p.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE p.created_at >= DATE_SUB(NOW(), INTERVAL " + timeWindow + " HOUR) " +
                     "GROUP BY c.customer_id HAVING transaction_count > " + threshold +
                     " OR SUM(p.amount) > 10000 ORDER BY transaction_count DESC";
        PreparedStatement stmt = connection.prepareStatement(sql);
        return stmt.executeQuery();
    }

    public void flagTransactionForReview(String paymentId, String reason, String flaggedBy) throws SQLException {
        String sql = "UPDATE payments SET status = 'UNDER_REVIEW', review_reason = '" + reason +
                     "', flagged_at = NOW(), flagged_by = '" + flaggedBy + "' WHERE payment_id = '" + paymentId + "'";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.executeUpdate();
        
        String alertSql = "INSERT INTO fraud_alerts (payment_id, reason, severity, created_at) VALUES ('" +
                          paymentId + "', '" + reason + "', 'MEDIUM', NOW())";
        PreparedStatement alertStmt = connection.prepareStatement(alertSql);
        alertStmt.executeUpdate();
    }

    public ResultSet generateRevenueAnalytics(String groupBy, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT DATE_FORMAT(created_at, '" + groupBy + "') as period, " +
                     "COUNT(*) as transaction_count, SUM(amount) as total_revenue, " +
                     "AVG(amount) as avg_transaction, payment_method " +
                     "FROM payments WHERE status = 'COMPLETED' AND created_at BETWEEN '" + dateFrom + "' AND '" + dateTo +
                     "' GROUP BY period, payment_method ORDER BY period DESC";
        PreparedStatement stmt = connection.prepareStatement(sql);
        return stmt.executeQuery();
    }

    public void reconcilePayments(String merchantId, String settlementDate) throws SQLException {
        String sql = "SELECT p.*, o.order_id FROM payments p JOIN orders o ON p.order_id = o.order_id " +
                     "WHERE p.status = 'COMPLETED' AND DATE(p.created_at) = '" + settlementDate +
                     "' AND o.merchant_id = '" + merchantId + "'";
        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        double totalAmount = 0.0;
        while (rs.next()) {
            totalAmount += rs.getDouble("amount");
        }
        
        String reconcileSql = "INSERT INTO payment_reconciliation (merchant_id, settlement_date, total_amount, status) " +
                              "VALUES (?, ?, ?, 'PENDING')";
        PreparedStatement reconcileStmt = connection.prepareStatement(reconcileSql);
        reconcileStmt.setString(1, merchantId);
        reconcileStmt.setString(2, settlementDate);
        reconcileStmt.setDouble(3, totalAmount);
        reconcileStmt.executeUpdate();
    }
}
