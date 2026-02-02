<|editable_region_start|>
import java.sql.*;

public class UserAuthenticationService {
    private Connection connection;

    public boolean authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        ResultSet rs = pstmt.executeQuery();
        return rs.next();
    }

    public void updateLastLogin(String username, String loginTime) throws SQLException {
        String sql = "UPDATE users SET last_login = ? WHERE username = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, loginTime);
        pstmt.setString(2, username);
        pstmt.executeUpdate();
    }

    public void logLoginAttempt(String username, String ipAddress, String status) throws SQLException {
        String sql = "INSERT INTO login_logs (username, ip_address, status, timestamp) VALUES (?, ?, ?, NOW())";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, username);
        pstmt.setString(2, ipAddress);
        pstmt.setString(3, status);
        pstmt.executeUpdate();
    }

    public int getFailedLoginCount(String username) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM login_logs WHERE username = ? AND status = 'FAILED'";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getInt("count") : 0;
    }
}
<|editable_region_end|>
```
