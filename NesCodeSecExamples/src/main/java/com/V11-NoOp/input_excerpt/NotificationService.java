```<|start_of_file|>
<|editable_region_start|>
import java.sql.*;

public class NotificationService {
    private Connection connection;

    public void createNotification(String userId, String message, String type, String priority) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, message, type, priority, status, created_at) VALUES (?, ?, ?, ?, 'UNREAD', NOW())";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setString(2, message);
        pstmt.setString(3, type);
        pstmt.setString(4, priority);
        pstmt.executeUpdate();
    }

    public ResultSet getUnreadNotifications(String userId, String type) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = '" + userId + "' AND status = 'UNREAD'"<|user_cursor_is_here|>
        if (type != null && !type.isEmpty()) {
            sql += " AND type = '" + type + "'";
        }
        sql += " ORDER BY created_at DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void markAsRead(String notificationId, String userId) throws SQLException {
        String sql = "UPDATE notifications SET status = 'READ', read_at = NOW() " +
                     "WHERE notification_id = '" + notificationId + "' AND user_id = '" + userId + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void deleteOldNotifications(String userId, int daysOld) throws SQLException {
        String sql = "DELETE FROM notifications WHERE user_id = '" + userId + 
                     "' AND created_at < DATE_SUB(NOW(), INTERVAL " + daysOld + " DAY)";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public int getUnreadCount(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM notifications WHERE user_id = '" + userId + "' AND status = 'UNREAD'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        return rs.next() ? rs.getInt("count") : 0;
    }

    public void createBulkNotification(String targetSegment, String message, String type, String priority, String createdBy) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, message, type, priority, status, created_at) " +
                     "SELECT c.customer_id, '" + message + "', '" + type + "', '" + priority + "', 'UNREAD', NOW() " +
                     "FROM customers c " +
                     "JOIN customer_segment_assignments csa ON c.customer_id = csa.customer_id " +
                     "WHERE csa.segment_name = '" + targetSegment + "'";
        Statement stmt = connection.createStatement();
        int rowsCreated = stmt.executeUpdate(sql);
        
        String logSql = "INSERT INTO notification_campaigns (segment, message, type, created_by, recipients_count, created_at) " +
                        "VALUES ('" + targetSegment + "', '" + message + "', '" + type + "', '" + createdBy + "', " + rowsCreated + ", NOW())";
        Statement logStmt = connection.createStatement();
        logStmt.executeUpdate(logSql);
    }

    public void createFromTemplate(String userId, String templateId, String variables) throws SQLException {
        String templateSql = "SELECT template_body, type, priority FROM notification_templates WHERE template_id = '" + templateId + "'";
        Statement templateStmt = connection.createStatement();
        ResultSet rs = templateStmt.executeQuery(templateSql);
        
        if (rs.next()) {
            String message = rs.getString("template_body");
            String type = rs.getString("type");
            String priority = rs.getString("priority");
            
            String sql = "INSERT INTO notifications (user_id, message, type, priority, template_id, variables, status, created_at) " +
                         "VALUES ('" + userId + "', '" + message + "', '" + type + "', '" + priority + "', '" +
                         templateId + "', '" + variables + "', 'UNREAD', NOW())";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
    }

    public void scheduleNotification(String userId, String message, String type, String scheduledTime, String createdBy) throws SQLException {
        String sql = "INSERT INTO scheduled_notifications (user_id, message, type, scheduled_time, status, created_by, created_at) " +
                     "VALUES ('" + userId + "', '" + message + "', '" + type + "', '" + scheduledTime + "', 'PENDING', '" + createdBy + "', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet getNotificationPreferences(String userId) throws SQLException {
        String sql = "SELECT np.channel, np.enabled, np.frequency, np.quiet_hours_start, np.quiet_hours_end " +
                     "FROM notification_preferences np WHERE np.user_id = '" + userId + "'";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void updateNotificationPreferences(String userId, String channel, String enabled, String frequency) throws SQLException {
        String sql = "INSERT INTO notification_preferences (user_id, channel, enabled, frequency, updated_at) " +
                     "VALUES ('" + userId + "', '" + channel + "', " + enabled + ", '" + frequency + "', NOW()) " +
                     "ON DUPLICATE KEY UPDATE enabled = " + enabled + ", frequency = '" + frequency + "', updated_at = NOW()";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void sendPushNotification(String userId, String title, String body, String actionUrl, String iconUrl) throws SQLException {
        String sql = "INSERT INTO push_notifications (user_id, title, body, action_url, icon_url, status, created_at) " +
                     "VALUES ('" + userId + "', '" + title + "', '" + body + "', '" + actionUrl + "', '" + iconUrl + "', 'PENDING', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet getNotificationAnalytics(String type, String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT " +
                     "DATE(created_at) as notification_date, " +
                     "COUNT(*) as total_sent, " +
                     "SUM(CASE WHEN status = 'READ' THEN 1 ELSE 0 END) as total_read, " +
                     "SUM(CASE WHEN status = 'READ' THEN 1 ELSE 0 END) / COUNT(*) * 100 as read_rate, " +
                     "AVG(TIMESTAMPDIFF(MINUTE, created_at, read_at)) as avg_time_to_read " +
                     "FROM notifications " +
                     "WHERE type = '" + type + "' AND created_at BETWEEN '" + dateFrom + "' AND '" + dateTo +
                     "' GROUP BY notification_date ORDER BY notification_date DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void resendFailedNotifications(String notificationId, String channel) throws SQLException {
        String sql = "UPDATE notifications SET status = 'RETRY', retry_count = retry_count + 1, last_retry_at = NOW() " +
                     "WHERE notification_id = '" + notificationId + "' AND status = 'FAILED'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        
        String logSql = "INSERT INTO notification_delivery_log (notification_id, channel, attempt_count, status, timestamp) " +
                        "VALUES ('" + notificationId + "', '" + channel + "', (SELECT retry_count FROM notifications WHERE notification_id = '" +
                        notificationId + "'), 'RETRY', NOW())";
        Statement logStmt = connection.createStatement();
        logStmt.executeUpdate(logSql);
    }

    public void markAllAsRead(String userId, String type) throws SQLException {
        String sql = "UPDATE notifications SET status = 'READ', read_at = NOW() " +
                     "WHERE user_id = '" + userId + "' AND status = 'UNREAD'";
        
        if (type != null && !type.isEmpty()) {
            sql += " AND type = '" + type + "'";
        }
        
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }
}
<|editable_region_end|>
```