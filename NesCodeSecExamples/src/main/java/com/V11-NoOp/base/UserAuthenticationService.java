import java.sql.*;

public class UserAuthenticationService {
    private Connection connection;

    public boolean authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        return rs.next();
    }

    public void updateLastLogin(String username, String loginTime) throws SQLException {
        String sql = "UPDATE users SET last_login = '" + loginTime + "' WHERE username = '" + username + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void logLoginAttempt(String username, String ipAddress, String status) throws SQLException {
        String sql = "INSERT INTO login_logs (username, ip_address, status, timestamp) VALUES ('" + 
                     username + "', '" + ipAddress + "', '" + status + "', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public int getFailedLoginCount(String username) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM login_logs WHERE username = '" + username + "' AND status = 'FAILED'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        return rs.next() ? rs.getInt("count") : 0;
    }
}
