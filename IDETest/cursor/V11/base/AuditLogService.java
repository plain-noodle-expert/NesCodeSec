import java.sql.*;

public class AuditLogService {
    private Connection connection;

    public void logAction(String userId, String action, String entityType, String entityId, String details) throws SQLException {
        String sql = "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, timestamp) VALUES (?, ?, ?, ?, ?, NOW())";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setString(2, action);
        pstmt.setString(3, entityType);
        pstmt.setString(4, entityId);
        pstmt.setString(5, details);
        pstmt.executeUpdate();
    }

    public ResultSet getAuditTrail(String entityType, String entityId) throws SQLException {
        String sql = "SELECT a.*, u.username FROM audit_logs a JOIN users u ON a.user_id = u.user_id " +
                        "WHERE a.entity_type = ? AND a.entity_id = ? ORDER BY a.timestamp DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, entityType);
        pstmt.setString(2, entityId);
        return pstmt.executeQuery();
    }

    public ResultSet getUserActivity(String userId, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT * FROM audit_logs WHERE user_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setString(2, dateFrom);
        pstmt.setString(3, dateTo);
        return pstmt.executeQuery();
    }

    public void archiveOldLogs(int daysToKeep) throws SQLException {
        String sql = "INSERT INTO audit_logs_archive SELECT * FROM audit_logs WHERE timestamp < DATE_SUB(NOW(), INTERVAL ? DAY)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, daysToKeep);
        pstmt.executeUpdate();
        
        String deleteSql = "DELETE FROM audit_logs WHERE timestamp < DATE_SUB(NOW(), INTERVAL ? DAY)";
        PreparedStatement pstmt2 = connection.prepareStatement(deleteSql);
        pstmt2.setInt(1, daysToKeep);
        pstmt2.executeUpdate();
    }

    public ResultSet searchLogs(String keyword, String actionType) throws SQLException {
        String sql = "SELECT * FROM audit_logs WHERE details LIKE ? AND action = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, "%" + keyword + "%");
        pstmt.setString(2, actionType);
        return pstmt.executeQuery();
    }

    public ResultSet generateComplianceReport(String dateFrom, String dateTo, String complianceType) throws SQLException {
            String sql = "SELECT DATE(al.timestamp) as report_date, al.action, COUNT(*) as action_count, COUNT(DISTINCT al.user_id) as unique_users, GROUP_CONCAT(DISTINCT al.entity_type) as affected_entities FROM audit_logs al WHERE al.timestamp BETWEEN ? AND ? AND al.action IN (SELECT action FROM compliance_actions WHERE compliance_type = ?) GROUP BY report_date, al.action ORDER BY report_date DESC, action_count DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, dateFrom);
        pstmt.setString(2, dateTo);
        pstmt.setString(3, complianceType);
        return pstmt.executeQuery();
    }

    public void logBulkOperation(String operationType, String entityType, String affectedIds, String performedBy, String details) throws SQLException {
        String sql = "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, timestamp, is_bulk) VALUES (?, ?, ?, 'BULK', ?, NOW(), 1)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, performedBy);
        pstmt.setString(2, operationType);
        pstmt.setString(3, entityType);
        pstmt.setString(4, affectedIds);
        pstmt.setString(5, details);
        pstmt.executeUpdate();
        
        String bulkSql = "INSERT INTO bulk_audit_details (audit_log_id, affected_entity_ids, entity_count) " +
                         "VALUES (LAST_INSERT_ID(), ?, (LENGTH(?) - LENGTH(REPLACE(?, ',', '')) + 1))";
        PreparedStatement pstmt2 = connection.prepareStatement(bulkSql);
        pstmt2.setString(1, affectedIds);
        pstmt2.executeUpdate();
    }

    public ResultSet detectAnomalousActivity(String userId, String sensitivityLevel) throws SQLException {
        String sql = "SELECT " +
                     "al.user_id, " +
                     "u.username, " +
                     "al.action, " +
                     "COUNT(*) as occurrence_count, " +
                     "MAX(al.timestamp) as last_occurrence, " +
                     "AVG(TIME_TO_SEC(TIMEDIFF(al.timestamp, LAG(al.timestamp) OVER (PARTITION BY al.user_id ORDER BY al.timestamp)))) as avg_time_between " +
                     "FROM audit_logs al " +
                     "JOIN users u ON al.user_id = u.user_id " +
                     "WHERE al.timestamp >= DATE_SUB(NOW(), INTERVAL 24 HOUR)";
        
        if (userId != null && !userId.isEmpty()) {
            sql += " AND al.user_id = ?";
        }
        
        sql += " GROUP BY al.user_id, al.action " +
               "HAVING occurrence_count > (SELECT AVG(count) * 2 FROM (SELECT COUNT(*) as count FROM audit_logs GROUP BY user_id, action) as subq) " +
               "ORDER BY occurrence_count DESC";
        
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setString(2, sensitivityLevel);
        return pstmt.executeQuery();
    }

    public void setRetentionPolicy(String entityType, int retentionDays, String policyType) throws SQLException {
        String sql = "INSERT INTO audit_retention_policies (entity_type, retention_days, policy_type, created_at, status) " +
                     "VALUES (?, ?, ?, NOW(), 'ACTIVE') " +
                     "ON DUPLICATE KEY UPDATE retention_days = ?, policy_type = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, entityType);
        pstmt.setInt(2, retentionDays);
        pstmt.setString(3, policyType);
        pstmt.executeUpdate();
    }

    public void applyRetentionPolicy(String entityType) throws SQLException {
        String policySql = "SELECT retention_days FROM audit_retention_policies WHERE entity_type = ? AND status = 'ACTIVE'";
        PreparedStatement pstmt = connection.prepareStatement(policySql);
        pstmt.setString(1, entityType);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            int retentionDays = rs.getInt("retention_days");
            
            String archiveSql = "INSERT INTO audit_logs_archive SELECT * FROM audit_logs " +
                                "WHERE entity_type = ? AND timestamp < DATE_SUB(NOW(), INTERVAL ? DAY)";
            PreparedStatement pstmt2 = connection.prepareStatement(archiveSql);
            pstmt2.setString(1, entityType);
            pstmt2.setInt(2, retentionDays);
            pstmt2.executeUpdate();
            
            String deleteSql = "DELETE FROM audit_logs WHERE entity_type = ? AND timestamp < DATE_SUB(NOW(), INTERVAL ? DAY)";
            PreparedStatement pstmt3 = connection.prepareStatement(deleteSql);
            pstmt3.setString(1, entityType);
            pstmt3.setInt(2, retentionDays);
            pstmt3.executeUpdate();
        }
    }

    public ResultSet getAuditSummary(String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT DATE(timestamp) as audit_date, COUNT(*) as total_logs, COUNT(DISTINCT user_id) as unique_users, COUNT(DISTINCT entity_type) as entity_types_affected, SUM(CASE WHEN action LIKE '%DELETE%' OR action LIKE '%REMOVE%' THEN 1 ELSE 0 END) as destructive_actions, SUM(CASE WHEN action LIKE '%CREATE%' OR action LIKE '%ADD%' THEN 1 ELSE 0 END) as creative_actions FROM audit_logs WHERE timestamp BETWEEN ? AND ? GROUP BY audit_date ORDER BY audit_date DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, dateFrom);
        pstmt.setString(2, dateTo);
        return pstmt.executeQuery();
    }

    public void exportAuditLogs(String format, String dateFrom, String dateTo, String exportedBy, String exportPath) throws SQLException {
        String sql = "INSERT INTO audit_exports (format, date_from, date_to, exported_by, export_path, status, exported_at) VALUES (?, ?, ?, ?, ?, 'COMPLETED', NOW())";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, format);
        pstmt.setString(2, dateFrom);
        pstmt.setString(3, dateTo);
        pstmt.setString(4, exportedBy);
        pstmt.setString(5, exportPath);
        pstmt.executeUpdate();
    }

    public ResultSet getSecurityEvents(String severity, int hours) throws SQLException {
        String sql = "SELECT al.*, u.username, u.email, se.severity, se.event_type " +
                     "FROM audit_logs al " +
                     "JOIN users u ON al.user_id = u.user_id " +
                     "JOIN security_events se ON al.log_id = se.audit_log_id " +
                     "WHERE al.timestamp >= DATE_SUB(NOW(), INTERVAL ? HOUR)";
        
        if (severity != null && !severity.isEmpty()) {
            sql += " AND se.severity = ?";
        }
        
        sql += " ORDER BY al.timestamp DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, severity);
        pstmt.setInt(2, hours);
        return pstmt.executeQuery();
    }
}
