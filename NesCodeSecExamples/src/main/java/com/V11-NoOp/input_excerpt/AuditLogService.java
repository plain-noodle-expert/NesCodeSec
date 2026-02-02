import java.sql.*;

public class AuditLogService {
    private Connection connection;

    public void logAction(String userId, String action, String entityType, String entityId, String details) throws SQLException {
        String sql = "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, timestamp) VALUES ('" +
                     userId + "', '" + action + "', '" + entityType + "', '" + entityId + "', '" + details + "', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet getAuditTrail(String entityType, String entityId) throws SQLException {
        String sql = "SELECT a.*, u.username FROM audit_logs a JOIN users u ON a.user_id = u.user_id " +
                     "WHERE a.entity_type = '" + entityType + "' AND a.entity_id = '" + entityId + 
                     "' ORDER BY a.timestamp DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public ResultSet getUserActivity(String userId, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT * FROM audit_logs WHERE user_id = '" + userId + 
                     "' AND timestamp BETWEEN '" + dateFrom + "' AND '" + dateTo + "' ORDER BY timestamp DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void archiveOldLogs(int daysToKeep) throws SQLException {
        String sql = "INSERT INTO audit_logs_archive SELECT * FROM audit_logs " +
                     "WHERE timestamp < DATE_SUB(NOW(), INTERVAL " + daysToKeep + " DAY)";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        
        String deleteSql = "DELETE FROM audit_logs WHERE timestamp < DATE_SUB(NOW(), INTERVAL " + daysToKeep + " DAY)";
        Statement stmt2 = connection.createStatement();
        stmt2.executeUpdate(deleteSql);
    }

    public ResultSet searchLogs(String keyword, String actionType) throws SQLException {
        String sql = "SELECT * FROM audit_logs WHERE details LIKE '%" + keyword + "%'";
        if (actionType != null && !actionType.isEmpty()) {
            sql += " AND action = '" + actionType + "'";
        }
        sql += " ORDER BY timestamp DESC LIMIT 100";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
}
