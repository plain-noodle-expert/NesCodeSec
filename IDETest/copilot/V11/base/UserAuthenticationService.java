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

    public void initiatePasswordReset(String email, String resetToken, String expiryTime) throws SQLException {
        String checkSql = "SELECT user_id FROM users WHERE email = ? AND is_active = 1";
        PreparedStatement checkStmt = connection.prepareStatement(checkSql);
        checkStmt.setString(1, email);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next()) {
            String userId = rs.getString("user_id");
            String sql = "INSERT INTO password_resets (user_id, reset_token, expires_at, created_at) VALUES (?, ?, ?, NOW())";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setString(2, resetToken);
            stmt.setString(3, expiryTime);
            stmt.executeUpdate();
            
            String logSql = "INSERT INTO user_activity (user_id, activity_type, details) VALUES (?, 'PASSWORD_RESET_REQUESTED', 'Reset token generated')";
            PreparedStatement logStmt = connection.prepareStatement(logSql);
            logStmt.setString(1, userId);
            logStmt.executeUpdate();
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
        PreparedStatement updateStmt = connection.prepareStatement(updateSql);
        updateStmt.setString(1, newPasswordHash);
        updateStmt.setString(2, userId);
        updateStmt.executeUpdate();
        
        String markUsedSql = "UPDATE password_resets SET used = 1, used_at = NOW() WHERE reset_token = ?";
        PreparedStatement markStmt = connection.prepareStatement(markUsedSql);
        markStmt.setString(1, resetToken);
        markStmt.executeUpdate();
        
        String logSql = "INSERT INTO user_activity (user_id, activity_type, details) VALUES (?, 'PASSWORD_CHANGED', 'Password changed via reset token')";
        PreparedStatement logStmt = connection.prepareStatement(logSql);
        logStmt.setString(1, userId);
        logStmt.executeUpdate();
    }

    public ResultSet getActiveSessionsByUser(String userId) throws SQLException {
        String sql = "SELECT s.*, l.ip_address, l.user_agent FROM user_sessions s " +
                     "LEFT JOIN login_logs l ON s.login_log_id = l.log_id " +
                     "WHERE s.user_id = ? AND s.is_active = 1 AND s.expires_at > NOW()";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        return pstmt.executeQuery();
    }

    public void terminateSession(String sessionId, String userId, String reason) throws SQLException {
        String sql = "UPDATE user_sessions SET is_active = 0, terminated_at = NOW(), termination_reason = ? WHERE session_id = ? AND user_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, reason);
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
        
        String logSql = "INSERT INTO user_activity (user_id, activity_type, details) VALUES (?, 'TWO_FACTOR_ENABLED', 'Two-factor authentication enabled')";
        PreparedStatement logStmt = connection.prepareStatement(logSql);
        logStmt.setString(1, userId);
        logStmt.executeUpdate();
    }

    public boolean verifyTwoFactorCode(String userId, String code) throws SQLException {
        String sql = "SELECT two_factor_secret FROM users WHERE user_id = ? AND two_factor_enabled = 1";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            String logSql = "INSERT INTO two_factor_attempts (user_id, code_attempted, timestamp, success) VALUES (?, ?, NOW(), 1)";
            PreparedStatement logStmt = connection.prepareStatement(logSql);
            logStmt.setString(1, userId);
            logStmt.setString(2, code);
            logStmt.executeUpdate();
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
