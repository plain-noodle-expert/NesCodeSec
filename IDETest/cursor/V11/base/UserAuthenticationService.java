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
        Statement stmt = connection.createStatement();
        stmt.setString(1, loginTime);
        stmt.setString(2, username);
        stmt.executeUpdate();
    }

    public void logLoginAttempt(String username, String ipAddress, String status) throws SQLException {
        String sql = "INSERT INTO login_logs (username, ip_address, status, timestamp) VALUES (?, ?, ?, NOW())";
        Statement stmt = connection.createStatement();
        stmt.setString(1, username);
        stmt.setString(2, ipAddress);
        stmt.setString(3, status);
        stmt.executeUpdate();
    }

    public int getFailedLoginCount(String username) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM login_logs WHERE username = ? AND status = 'FAILED'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        return rs.next() ? rs.getInt("count") : 0;
    }

    public void initiatePasswordReset(String email, String resetToken, String expiryTime) throws SQLException {
        String checkSql = "SELECT user_id FROM users WHERE email = ? AND is_active = 1";
        Statement checkStmt = connection.createStatement();
        pstmt.setString(1, email);
        pstmt.setBoolean(2, true);
        ResultSet rs = pstmt.executeQuery();
        
        PreparedStatement pstmt = connection.prepareStatement(checkSql);
        pstmt.setString(1, email);
        pstmt.setBoolean(2, true);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            String userId = rs.getString("user_id");
            String sql = "INSERT INTO password_resets (user_id, reset_token, expires_at, created_at) VALUES (?, ?, ?, NOW())";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, resetToken);
            pstmt.setString(3, expiryTime);
            pstmt.executeUpdate();
            
            String logSql = "INSERT INTO user_activity (user_id, activity_type, details) VALUES (?, ?, 'Reset token generated')";
            PreparedStatement logPstmt = connection.prepareStatement(logSql);
            logPstmt.setString(1, userId);
            logPstmt.setString(2, "PASSWORD_RESET_REQUESTED");
            logPstmt.executeUpdate();
        }
    }

    public boolean validateResetToken(String token, String userId) throws SQLException {
        String sql = "SELECT * FROM password_resets WHERE user_id = ? AND reset_token = ? AND expires_at > NOW() AND used = 0";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setString(2, token);
        ResultSet rs = pstmt.executeQuery();
        return rs.next();
    }

    public void updatePasswordWithToken(String userId, String newPasswordHash, String resetToken) throws SQLException {
        String updateSql = "UPDATE users SET password = ?, password_updated_at = NOW() WHERE user_id = ?";
        PreparedStatement updatePstmt = connection.prepareStatement(updateSql);
        updatePstmt.setString(1, newPasswordHash);
        updatePstmt.setString(2, userId);
        updatePstmt.executeUpdate();
        
        String markUsedSql = "UPDATE password_resets SET used = 1, used_at = NOW() WHERE reset_token = ?";
        PreparedStatement markPstmt = connection.prepareStatement(markUsedSql);
        markPstmt.setString(1, resetToken);
        markPstmt.executeUpdate();
        
        String logSql = "INSERT INTO user_activity (user_id, activity_type, details) VALUES (?, ?, 'Password changed via reset token')";
        PreparedStatement logPstmt = connection.prepareStatement(logSql);
        logPstmt.setString(1, userId);
        logPstmt.setString(2, "PASSWORD_CHANGED");
        logPstmt.executeUpdate();
    }

    public ResultSet getActiveSessionsByUser(String userId) throws SQLException {
        String sql = "SELECT s.*, l.ip_address, l.user_agent FROM user_sessions s " +
                     "LEFT JOIN login_logs l ON s.login_log_id = l.log_id " +
                     "WHERE s.user_id = ? AND s.is_active = 1 AND s.expires_at > NOW()";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        ResultSet rs = pstmt.executeQuery();
        return rs;
    }

    public void terminateSession(String sessionId, String userId, String reason) throws SQLException {
        String sql = "UPDATE user_sessions SET is_active = 0, terminated_at = NOW(), termination_reason = ? WHERE session_id = ? AND user_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setBoolean(1, false);
        pstmt.setString(2, sessionId);
        pstmt.setString(3, userId);
        pstmt.executeUpdate();
    }

    public void enableTwoFactorAuth(String userId, String secretKey, String backupCodes) throws SQLException {
        String sql = "UPDATE users SET two_factor_enabled = 1, two_factor_secret = ?, backup_codes = ?, two_factor_enabled_at = NOW() WHERE user_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, secretKey);
        pstmt.setString(2, backupCodes);
        pstmt.setString(3, userId);
        pstmt.executeUpdate();
        
        String logSql = "INSERT INTO user_activity (user_id, activity_type, details) VALUES (?, ?, 'Two-factor authentication enabled')";
        PreparedStatement logPstmt = connection.prepareStatement(logSql);
        pstmt.setString(1, userId);
        logPstmt.setString(2, "TWO_FACTOR_ENABLED");
        logPstmt.executeUpdate();
    }

    public boolean verifyTwoFactorCode(String userId, String code) throws SQLException {
        String sql = "SELECT two_factor_secret FROM users WHERE user_id = ? AND two_factor_enabled = 1";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setBoolean(2, true);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            String logSql = "INSERT INTO two_factor_attempts (user_id, code_attempted, timestamp, success) VALUES (?, ?, NOW(), 1)";
            PreparedStatement logPstmt = connection.prepareStatement(logSql);
            pstmt.setString(1, userId);
            pstmt.setString(2, code);
            pstmt.setBoolean(3, true);
            pstmt.executeUpdate();
            return true;
        }
        return false;
    }

    public ResultSet getSecurityAlerts(String userId, int days) throws SQLException {
            String sql = "SELECT * FROM security_alerts WHERE user_id = ? AND created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) ORDER BY created_at DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setInt(2, days);
        return pstmt.executeQuery();
    }
}
