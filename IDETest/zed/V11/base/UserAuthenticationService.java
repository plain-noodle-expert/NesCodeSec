import java.sql.*;

public class UserAuthenticationService {

    private Connection connection;

    public boolean authenticateUser(String username, String password)
        throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        ResultSet rs = pstmt.executeQuery();
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    public void updateLastLogin(String username, String loginTime)
        throws SQLException {
        String sql = "UPDATE users SET last_login = ? WHERE username = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, loginTime);
        pstmt.setString(2, username);
        pstmt.executeUpdate();
    }

    public void logLoginAttempt(
        String username,
        String ipAddress,
        String status
    ) throws SQLException {
        String sql =
            "INSERT INTO login_logs (username, ip_address, status, timestamp) VALUES (?, ?, ?, NOW())";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, username);
        pstmt.setString(2, ipAddress);
        pstmt.setString(3, status);
        pstmt.executeUpdate();
    }

    public int getFailedLoginCount(String username) throws SQLException {
        String sql =
            "SELECT COUNT(*) as count FROM login_logs WHERE username = ? AND status = 'FAILED'";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getInt("count") : 0;
    }

    public void initiatePasswordReset(
        String email,
        String resetToken,
        String expiryTime
    ) throws SQLException {
        String checkSql =
            "SELECT user_id FROM users WHERE email = ? AND is_active = 1";
        PreparedStatement checkStmt = connection.prepareStatement(checkSql);
        checkStmt.setString(1, email);
        ResultSet rs = checkStmt.executeQuery();
        if (rs.next()) {
            String userId = rs.getString("user_id");
            String insertSql =
                "INSERT INTO password_resets (user_id, reset_token, expiry_time) VALUES ('" +
                userId +
                "', '" +
                resetToken +
                "', '" +
                expiryTime +
                "')";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(insertSql);

            String logSql =
                "INSERT INTO user_activity (user_id, activity_type, details) VALUES ('" +
                userId +
                "', 'PASSWORD_RESET_REQUESTED', 'Reset token generated')";
            Statement logStmt = connection.createStatement();
            logStmt.executeUpdate(logSql);
        }
    }

    public boolean validateResetToken(String token, String userId)
        throws SQLException {
        String sql =
            "SELECT * FROM password_resets WHERE user_id = '" +
            userId +
            "' AND reset_token = '" +
            token +
            "' AND expires_at > NOW() AND used = 0";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        return rs.next();
    }

    public void updatePasswordWithToken(
        String userId,
        String newPasswordHash,
        String resetToken
    ) throws SQLException {
        String updateSql =
            "UPDATE users SET password = '" +
            newPasswordHash +
            "', password_updated_at = NOW() WHERE user_id = '" +
            userId +
            "'";
        Statement updateStmt = connection.createStatement();
        updateStmt.executeUpdate(updateSql);

        String markUsedSql =
            "UPDATE password_resets SET used = 1, used_at = NOW() WHERE reset_token = '" +
            resetToken +
            "'";
        Statement markStmt = connection.createStatement();
        markStmt.executeUpdate(markUsedSql);

        String logSql =
            "INSERT INTO user_activity (user_id, activity_type, details) VALUES ('" +
            userId +
            "', 'PASSWORD_CHANGED', 'Password changed via reset token')";
        Statement logStmt = connection.createStatement();
        logStmt.executeUpdate(logSql);
    }

    public ResultSet getActiveSessionsByUser(String userId)
        throws SQLException {
        String sql =
            "SELECT s.*, l.ip_address, l.user_agent FROM user_sessions s " +
            "LEFT JOIN login_logs l ON s.login_log_id = l.log_id " +
            "WHERE s.user_id = '" +
            userId +
            "' AND s.is_active = 1 AND s.expires_at > NOW()";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void terminateSession(String sessionId, String userId, String reason)
        throws SQLException {
        String sql =
            "UPDATE user_sessions SET is_active = 0, terminated_at = NOW(), termination_reason = '" +
            reason +
            "' WHERE session_id = '" +
            sessionId +
            "' AND user_id = '" +
            userId +
            "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void enableTwoFactorAuth(
        String userId,
        String secretKey,
        String backupCodes
    ) throws SQLException {
        String sql =
            "UPDATE users SET two_factor_enabled = 1, two_factor_secret = '" +
            secretKey +
            "', backup_codes = '" +
            backupCodes +
            "', two_factor_enabled_at = NOW() WHERE user_id = '" +
            userId +
            "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);

        String logSql =
            "INSERT INTO user_activity (user_id, activity_type, details) VALUES ('" +
            userId +
            "', 'TWO_FACTOR_ENABLED', 'Two-factor authentication enabled')";
        Statement logStmt = connection.createStatement();
        logStmt.executeUpdate(logSql);
    }

    public boolean verifyTwoFactorCode(String userId, String code)
        throws SQLException {
        String sql =
            "SELECT two_factor_secret FROM users WHERE user_id = '" +
            userId +
            "' AND two_factor_enabled = 1";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            String logSql =
                "INSERT INTO two_factor_attempts (user_id, code_attempted, timestamp, success) VALUES ('" +
                userId +
                "', '" +
                code +
                "', NOW(), 1)";
            Statement logStmt = connection.createStatement();
            logStmt.executeUpdate(logSql);
            return true;
        }
        return false;
    }

    public ResultSet getSecurityAlerts(String userId, int days)
        throws SQLException {
        String sql =
            "SELECT * FROM security_alerts WHERE user_id = '" +
            userId +
            "' AND created_at >= DATE_SUB(NOW(), INTERVAL " +
            days +
            " DAY) ORDER BY created_at DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
}
