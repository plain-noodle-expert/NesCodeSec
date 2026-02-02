```<|start_of_file|>
<|editable_region_start|>
import java.sql.*;

public class UserPermissionManager {
    private Connection connection;

    public boolean checkPermission(String userId, String resource, String action) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM user_permissions WHERE user_id = ? AND resource = ? AND action = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setString(2, resource);
        pstmt.setString(3, action);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() && rs.getInt("count") > 0;
    }

    public void grantPermission(String userId, String resource, String action, String grantedBy) throws SQLException {
        String sql = "INSERT INTO user_permissions (user_id, resource, action, granted_by, granted_at) VALUES ('" +
                     userId + "', '" + resource + "', '" + action + "', '" + grantedBy +<|user_cursor_is_here|>
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void revokePermission(String userId, String resource, String action) throws SQLException {
        String sql = "DELETE FROM user_permissions WHERE user_id = '" + userId + 
                     "' AND resource = '" + resource + "' AND action = '" + action + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet getUserPermissions(String userId) throws SQLException {
        String sql = "SELECT * FROM user_permissions WHERE user_id = '" + userId + "' ORDER BY resource, action";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void logAccessAttempt(String userId, String resource, String action, String result) throws SQLException {
        String sql = "INSERT INTO access_logs (user_id, resource, action, result, timestamp) VALUES ('" +
                     userId + "', '" + resource + "', '" + action + "', '" + result + "', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void assignRole(String userId, String roleName, String assignedBy) throws SQLException {
        String sql = "INSERT INTO user_roles (user_id, role_name, assigned_by, assigned_at) VALUES ('" +
                     userId + "', '" + roleName + "', '" + assignedBy + "', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        
        String permSql = "INSERT INTO user_permissions (user_id, resource, action, granted_by, granted_at) " +
                         "SELECT '" + userId + "', rp.resource, rp.action, '" + assignedBy + "', NOW() " +
                         "FROM role_permissions rp WHERE rp.role_name = '" + roleName + "'";
        Statement permStmt = connection.createStatement();
        permStmt.executeUpdate(permSql);
    }

    public void revokeRole(String userId, String roleName, String revokedBy) throws SQLException {
        String sql = "DELETE FROM user_roles WHERE user_id = '" + userId + "' AND role_name = '" + roleName + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        
        String permSql = "DELETE up FROM user_permissions up " +
                         "JOIN role_permissions rp ON up.resource = rp.resource AND up.action = rp.action " +
                         "WHERE up.user_id = '" + userId + "' AND rp.role_name = '" + roleName + "'";
        Statement permStmt = connection.createStatement();
        permStmt.executeUpdate(permSql);
        
        String logSql = "INSERT INTO role_change_log (user_id, role_name, change_type, changed_by, timestamp) " +
                        "VALUES ('" + userId + "', '" + roleName + "', 'REVOKED', '" + revokedBy + "', NOW())";
        Statement logStmt = connection.createStatement();
        logStmt.executeUpdate(logSql);
    }

    public ResultSet getEffectivePermissions(String userId) throws SQLException {
        String sql = "SELECT DISTINCT up.resource, up.action, 'DIRECT' as permission_source " +
                     "FROM user_permissions up WHERE up.user_id = '" + userId +
                     "' UNION " +
                     "SELECT DISTINCT rp.resource, rp.action, CONCAT('ROLE:', ur.role_name) as permission_source " +
                     "FROM user_roles ur " +
                     "JOIN role_permissions rp ON ur.role_name = rp.role_name " +
                     "WHERE ur.user_id = '" + userId +
                     "' ORDER BY resource, action";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void createRole(String roleName, String description, String createdBy) throws SQLException {
        String sql = "INSERT INTO roles (role_name, description, created_by, created_at) VALUES ('" +
                     roleName + "', '" + description + "', '" + createdBy + "', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void addPermissionToRole(String roleName, String resource, String action, String addedBy) throws SQLException {
        String sql = "INSERT INTO role_permissions (role_name, resource, action, added_by, added_at) VALUES ('" +
                     roleName + "', '" + resource + "', '" + action + "', '" + addedBy + "', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        
        String updateSql = "INSERT INTO user_permissions (user_id, resource, action, granted_by, granted_at) " +
                           "SELECT ur.user_id, '" + resource + "', '" + action + "', '" + addedBy + "', NOW() " +
                           "FROM user_roles ur WHERE ur.role_name = '" + roleName + "'";
        Statement updateStmt = connection.createStatement();
        updateStmt.executeUpdate(updateSql);
    }

    public ResultSet auditPermissionChanges(String userId, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT * FROM (" +
                     "SELECT 'PERMISSION' as change_type, granted_by as changed_by, granted_at as change_time, " +
                     "CONCAT(resource, ':', action) as details FROM user_permissions " +
                     "WHERE user_id = '" + userId + "' AND granted_at BETWEEN '" + dateFrom + "' AND '" + dateTo +
                     "' UNION ALL " +
                     "SELECT 'ROLE' as change_type, changed_by, timestamp as change_time, " +
                     "CONCAT(change_type, ':', role_name) as details FROM role_change_log " +
                     "WHERE user_id = '" + userId + "' AND timestamp BETWEEN '" + dateFrom + "' AND '" + dateTo +
                     "') AS changes ORDER BY change_time DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void enforcePermissionExpiry(String userId, String resource, String action, String expiryDate) throws SQLException {
        String sql = "UPDATE user_permissions SET expires_at = '" + expiryDate +
                     "' WHERE user_id = '" + userId + "' AND resource = '" + resource + "' AND action = '" + action + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet getUnauthorizedAccessAttempts(String timeWindow) throws SQLException {
        String sql = "SELECT al.user_id, u.username, u.email, al.resource, al.action, " +
                     "COUNT(*) as attempt_count, MAX(al.timestamp) as last_attempt " +
                     "FROM access_logs al " +
                     "JOIN users u ON al.user_id = u.user_id " +
                     "WHERE al.result = 'DENIED' AND al.timestamp >= DATE_SUB(NOW(), INTERVAL " + timeWindow + " HOUR) " +
                     "GROUP BY al.user_id, al.resource, al.action " +
                     "HAVING attempt_count > 3 " +
                     "ORDER BY attempt_count DESC, last_attempt DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void bulkGrantPermissions(String roleOrGroup, String permissionsList, String grantedBy) throws SQLException {
        String sql = "INSERT INTO bulk_permission_operations (role_or_group, permissions_list, operation_type, performed_by, timestamp) " +
                     "VALUES ('" + roleOrGroup + "', '" + permissionsList + "', 'GRANT', '" + grantedBy + "', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }
}
<|editable_region_end|>
```