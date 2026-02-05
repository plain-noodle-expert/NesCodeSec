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

    public void processBatchPayments(String batchId, String processedBy) throws SQLException {
        String selectSql = "SELECT * FROM payment_batch WHERE batch_id = '" + batchId + "' AND status = 'PENDING'";
        Statement selectStmt = connection.createStatement();
        ResultSet rs = selectStmt.executeQuery(selectSql);
        
        while (rs.next()) {
            String paymentId = rs.getString("payment_id");
            String updateSql = "UPDATE payments SET status = 'PROCESSING', processed_at = NOW(), processed_by = '" +
                               processedBy + "' WHERE payment_id = '" + paymentId + "'";
            Statement updateStmt = connection.createStatement();
            updateStmt.executeUpdate(updateSql);
        }
        
        String batchUpdateSql = "UPDATE payment_batches SET status = 'PROCESSED', processed_at = NOW() WHERE batch_id = '" + batchId + "'";
        Statement batchStmt = connection.createStatement();
        batchStmt.executeUpdate(batchUpdateSql);
    }

    public void setupRecurringPayment(String customerId, String planId, double amount, String frequency, String startDate) throws SQLException {
        String sql = "INSERT INTO recurring_payments (customer_id, plan_id, amount, frequency, next_charge_date, status) " +
                     "VALUES ('" + customerId + "', '" + planId + "', " + amount + ", '" + frequency +
                     "', '" + startDate + "', 'ACTIVE')";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        
        String logSql = "INSERT INTO payment_logs (customer_id, action, details) VALUES ('" +
                        customerId + "', 'RECURRING_SETUP', 'Recurring payment scheduled for " + frequency + "')";
        Statement logStmt = connection.createStatement();
        logStmt.executeUpdate(logSql);
    }

    public void processInstallment(String orderId, int installmentNumber, double amount, String dueDate) throws SQLException {
        String checkSql = "SELECT * FROM installment_plans WHERE order_id = '" + orderId + "'";
        Statement checkStmt = connection.createStatement();
        ResultSet rs = checkStmt.executeQuery(checkSql);
        
        if (rs.next()) {
            String sql = "INSERT INTO installment_payments (order_id, installment_number, amount, due_date, status) " +
                         "VALUES ('" + orderId + "', " + installmentNumber + ", " + amount + ", '" + dueDate + "', 'PENDING')";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
    }

    public ResultSet detectSuspiciousTransactions(String threshold, String timeWindow) throws SQLException {
        String sql = "SELECT p.*, c.customer_id, c.email, COUNT(*) as transaction_count " +
                     "FROM payments p JOIN orders o ON p.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE p.created_at >= DATE_SUB(NOW(), INTERVAL " + timeWindow + " HOUR) " +
                     "GROUP BY c.customer_id HAVING transaction_count > " + threshold +
                     " OR SUM(p.amount) > 10000 ORDER BY transaction_count DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void flagTransactionForReview(String paymentId, String reason, String flaggedBy) throws SQLException {
        String sql = "UPDATE payments SET status = 'UNDER_REVIEW', review_reason = '" + reason +
                     "', flagged_at = NOW(), flagged_by = '" + flaggedBy + "' WHERE payment_id = '" + paymentId + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        
        String alertSql = "INSERT INTO fraud_alerts (payment_id, reason, severity, created_at) VALUES ('" +
                          paymentId + "', '" + reason + "', 'MEDIUM', NOW())";
        Statement alertStmt = connection.createStatement();
        alertStmt.executeUpdate(alertSql);
    }

    public ResultSet generateRevenueAnalytics(String groupBy, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT DATE_FORMAT(created_at, '" + groupBy + "') as period, " +
                     "COUNT(*) as transaction_count, SUM(amount) as total_revenue, " +
                     "AVG(amount) as avg_transaction, payment_method " +
                     "FROM payments WHERE status = 'COMPLETED' AND created_at BETWEEN '" + dateFrom + "' AND '" + dateTo +
                     "' GROUP BY period, payment_method ORDER BY period DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void reconcilePayments(String merchantId, String settlementDate) throws SQLException {
        String sql = "SELECT p.*, o.order_id FROM payments p JOIN orders o ON p.order_id = o.order_id " +
                     "WHERE p.status = 'COMPLETED' AND DATE(p.created_at) = '" + settlementDate +
                     "' AND o.merchant_id = '" + merchantId + "'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        double totalAmount = 0.0;
        while (rs.next()) {
            totalAmount += rs.getDouble("amount");
        }
        
        String reconcileSql = "INSERT INTO payment_reconciliation (merchant_id, settlement_date, total_amount, status) " +
                              "VALUES ('" + merchantId + "', '" + settlementDate + "', " + totalAmount + ", 'PENDING')";
        Statement reconcileStmt = connection.createStatement();
        reconcileStmt.executeUpdate(reconcileSql);
    }
}
