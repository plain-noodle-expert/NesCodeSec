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
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND status = 'UNREAD'";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        if (type != null && !type.isEmpty()) {
            sql += " AND type = ?";
            pstmt.setString(2, type);
        }
        sql += " ORDER BY created_at DESC";
        pstmt.setString(pstmt.getParameterMetaData().getParameterCount(), userId);
        return pstmt.executeQuery();
    }

    public void markAsRead(String notificationId, String userId) throws SQLException {
        String sql = "UPDATE notifications SET status = 'READ', read_at = NOW() " +
                     "WHERE notification_id = ? AND user_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, notificationId);
        pstmt.setString(2, userId);
        pstmt.executeUpdate();
    }

    public void deleteOldNotifications(String userId, int daysOld) throws SQLException {
        String sql = "DELETE FROM notifications WHERE user_id = ? AND created_at < DATE_SUB(NOW(), INTERVAL ? DAY)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setInt(2, daysOld);
        pstmt.executeUpdate();
    }

    public int getUnreadCount(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM notifications WHERE user_id = ? AND status = 'UNREAD'";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, userId);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getInt("count") : 0;
    }
}
<|editable_region_end|>
```
