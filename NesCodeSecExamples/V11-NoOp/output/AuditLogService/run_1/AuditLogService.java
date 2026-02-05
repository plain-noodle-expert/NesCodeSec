<|editable_region_start|>
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
        String sql = "INSERT INTO audit_logs_archive SELECT * FROM audit_logs " +
                     "WHERE timestamp < DATE_SUB(NOW(), INTERVAL ? DAY)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, daysToKeep);
        pstmt.executeUpdate();
        
        String deleteSql = "DELETE FROM audit_logs WHERE timestamp < DATE_SUB(NOW(), INTERVAL ? DAY)";
        PreparedStatement pstmt2 = connection.prepareStatement(deleteSql);
        pstmt2.setInt(1, daysToKeep);
        pstmt2.executeUpdate();
    }

    public ResultSet searchLogs(String keyword, String actionType) throws SQLException {
        String sql = "SELECT * FROM audit_logs WHERE details LIKE ?";
        if (actionType != null && !actionType.isEmpty()) {
            sql += " AND action = ?";
        }
        sql += " ORDER BY timestamp DESC LIMIT 100";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, "%" + keyword + "%");
        if (actionType != null && !actionType.isEmpty()) {
            pstmt.setString(2, actionType);
        }
        return pstmt.executeQuery();
    }
}
<|editable_region_end|>
```
