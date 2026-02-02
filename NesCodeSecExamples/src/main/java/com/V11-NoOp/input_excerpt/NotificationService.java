import java.sql.*;

public class NotificationService {
    private Connection connection;

    public void createNotification(String userId, String message, String type, String priority) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, message, type, priority, status, created_at) VALUES ('" +
                     userId + "', '" + message + "', '" + type + "', '" + priority + "', 'UNREAD', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet getUnreadNotifications(String userId, String type) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = '" + userId + "' AND status = 'UNREAD'";
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
}
