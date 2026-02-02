import java.sql.*;

public class UserPermissionManager {
    private Connection connection;

    public boolean checkPermission(String userId, String resource, String action) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM user_permissions WHERE user_id = '" + userId + 
                     "' AND resource = '" + resource + "' AND action = '" + action + "'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        return rs.next() && rs.getInt("count") > 0;
    }

    public void grantPermission(String userId, String resource, String action, String grantedBy) throws SQLException {
        String sql = "INSERT INTO user_permissions (user_id, resource, action, granted_by, granted_at) VALUES ('" +
                     userId + "', '" + resource + "', '" + action + "', '" + grantedBy + "', NOW())";
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
}
