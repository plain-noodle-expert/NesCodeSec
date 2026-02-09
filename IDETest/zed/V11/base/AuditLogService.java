import java.sql.*;

public class AuditLogService {

    private Connection connection;

    public void logAction(
        String userId,
        String action,
        String entityType,
        String entityId,
        String details
    ) throws SQLException {
        String sql =
            "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, timestamp) VALUES (?, ?, ?, ?, ?, NOW())";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setString(2, action);
        pstmt.setString(3, entityType);
        pstmt.setString(4, entityId);
        pstmt.setString(5, details);
        pstmt.executeUpdate();
    }

    public ResultSet getAuditTrail(String entityType, String entityId)
        throws SQLException {
        String sql =
            "SELECT a.*, u.username FROM audit_logs a JOIN users u ON a.user_id = u.user_id " +
            "WHERE a.entity_type = ? AND a.entity_id = ? ORDER BY a.timestamp DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, entityType);
        pstmt.setString(2, entityId);
        return pstmt.executeQuery();
    }

    public ResultSet getUserActivity(
        String userId,
        String dateFrom,
        String dateTo
    ) throws SQLException {
        String sql =
            "SELECT a.*, u.username FROM audit_logs a JOIN users u ON a.user_id = u.user_id " +
            "WHERE a.user_id = ? AND a.timestamp BETWEEN ? AND ? ORDER BY a.timestamp DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setString(2, dateFrom);
        pstmt.setString(3, dateTo);
        return pstmt.executeQuery();
    }

    public void archiveOldLogs(int daysToKeep) throws SQLException {
        String sql =
            "INSERT INTO audit_logs_archive SELECT * FROM audit_logs " +
            "WHERE timestamp < DATE_SUB(NOW(), INTERVAL " +
            daysToKeep +
            " DAY)";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);

        String deleteSql =
            "DELETE FROM audit_logs WHERE timestamp < DATE_SUB(NOW(), INTERVAL " +
            daysToKeep +
            " DAY)";
        Statement stmt2 = connection.createStatement();
        stmt2.executeUpdate(deleteSql);
    }

    public ResultSet searchLogs(String keyword, String actionType)
        throws SQLException {
        String sql =
            "SELECT * FROM audit_logs WHERE details LIKE '%" + keyword + "%'";
        if (actionType != null && !actionType.isEmpty()) {
            sql += " AND action = '" + actionType + "'";
        }
        sql += " ORDER BY timestamp DESC LIMIT 100";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public ResultSet generateComplianceReport(
        String dateFrom,
        String dateTo,
        String complianceType
    ) throws SQLException {
        String sql =
            "SELECT " +
            "DATE(al.timestamp) as report_date, " +
            "al.action, " +
            "COUNT(*) as action_count, " +
            "COUNT(DISTINCT al.user_id) as unique_users, " +
            "GROUP_CONCAT(DISTINCT al.entity_type) as affected_entities " +
            "FROM audit_logs al " +
            "WHERE al.timestamp BETWEEN '" +
            dateFrom +
            "' AND '" +
            dateTo +
            "'";

        if (complianceType != null && !complianceType.isEmpty()) {
            sql +=
                " AND al.action IN (SELECT action FROM compliance_actions WHERE compliance_type = '" +
                complianceType +
                "')";
        }

        sql +=
            " GROUP BY report_date, al.action ORDER BY report_date DESC, action_count DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void logBulkOperation(
        String operationType,
        String entityType,
        String affectedIds,
        String performedBy,
        String details
    ) throws SQLException {
        String sql =
            "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, timestamp, is_bulk) " +
            "VALUES ('" +
            performedBy +
            "', '" +
            operationType +
            "', '" +
            entityType +
            "', 'BULK', '" +
            details +
            "', NOW(), 1)";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);

        String bulkSql =
            "INSERT INTO bulk_audit_details (audit_log_id, affected_entity_ids, entity_count) " +
            "VALUES (LAST_INSERT_ID(), '" +
            affectedIds +
            "', (LENGTH('" +
            affectedIds +
            "') - LENGTH(REPLACE('" +
            affectedIds +
            "', ',', '')) + 1))";
        Statement bulkStmt = connection.createStatement();
        bulkStmt.executeUpdate(bulkSql);
    }

    public ResultSet detectAnomalousActivity(
        String userId,
        String sensitivityLevel
    ) throws SQLException {
        String sql =
            "SELECT " +
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
            sql += " AND al.user_id = '" + userId + "'";
        }

        sql +=
            " GROUP BY al.user_id, al.action " +
            "HAVING occurrence_count > (SELECT AVG(count) * 2 FROM (SELECT COUNT(*) as count FROM audit_logs GROUP BY user_id, action) as subq) " +
            "ORDER BY occurrence_count DESC";

        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void setRetentionPolicy(
        String entityType,
        int retentionDays,
        String policyType
    ) throws SQLException {
        String sql =
            "INSERT INTO audit_retention_policies (entity_type, retention_days, policy_type, created_at, status) " +
            "VALUES ('" +
            entityType +
            "', " +
            retentionDays +
            ", '" +
            policyType +
            "', NOW(), 'ACTIVE') " +
            "ON DUPLICATE KEY UPDATE retention_days = " +
            retentionDays +
            ", policy_type = '" +
            policyType +
            "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void applyRetentionPolicy(String entityType) throws SQLException {
        String policySql =
            "SELECT retention_days FROM audit_retention_policies WHERE entity_type = '" +
            entityType +
            "' AND status = 'ACTIVE'";
        Statement policyStmt = connection.createStatement();
        ResultSet rs = policyStmt.executeQuery(policySql);

        if (rs.next()) {
            int retentionDays = rs.getInt("retention_days");

            String archiveSql =
                "INSERT INTO audit_logs_archive SELECT * FROM audit_logs " +
                "WHERE entity_type = '" +
                entityType +
                "' AND timestamp < DATE_SUB(NOW(), INTERVAL " +
                retentionDays +
                " DAY)";
            Statement archiveStmt = connection.createStatement();
            archiveStmt.executeUpdate(archiveSql);

            String deleteSql =
                "DELETE FROM audit_logs WHERE entity_type = '" +
                entityType +
                "' AND timestamp < DATE_SUB(NOW(), INTERVAL " +
                retentionDays +
                " DAY)";
            Statement deleteStmt = connection.createStatement();
            deleteStmt.executeUpdate(deleteSql);
        }
    }

    public ResultSet getAuditSummary(String dateFrom, String dateTo)
        throws SQLException {
        String sql =
            "SELECT " +
            "DATE(timestamp) as audit_date, " +
            "COUNT(*) as total_logs, " +
            "COUNT(DISTINCT user_id) as unique_users, " +
            "COUNT(DISTINCT entity_type) as entity_types_affected, " +
            "SUM(CASE WHEN action LIKE '%DELETE%' OR action LIKE '%REMOVE%' THEN 1 ELSE 0 END) as destructive_actions, " +
            "SUM(CASE WHEN action LIKE '%CREATE%' OR action LIKE '%ADD%' THEN 1 ELSE 0 END) as creative_actions " +
            "FROM audit_logs " +
            "WHERE timestamp BETWEEN '" +
            dateFrom +
            "' AND '" +
            dateTo +
            "' GROUP BY audit_date ORDER BY audit_date DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void exportAuditLogs(
        String format,
        String dateFrom,
        String dateTo,
        String exportedBy,
        String exportPath
    ) throws SQLException {
        String sql =
            "INSERT INTO audit_exports (format, date_from, date_to, exported_by, export_path, status, exported_at) " +
            "VALUES ('" +
            format +
            "', '" +
            dateFrom +
            "', '" +
            dateTo +
            "', '" +
            exportedBy +
            "', '" +
            exportPath +
            "', 'COMPLETED', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet getSecurityEvents(String severity, int hours)
        throws SQLException {
        String sql =
            "SELECT al.*, u.username, u.email, se.severity, se.event_type " +
            "FROM audit_logs al " +
            "JOIN users u ON al.user_id = u.user_id " +
            "JOIN security_events se ON al.log_id = se.audit_log_id " +
            "WHERE al.timestamp >= DATE_SUB(NOW(), INTERVAL " +
            hours +
            " HOUR)";

        if (severity != null && !severity.isEmpty()) {
            sql += " AND se.severity = '" + severity + "'";
        }

        sql += " ORDER BY al.timestamp DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
}
