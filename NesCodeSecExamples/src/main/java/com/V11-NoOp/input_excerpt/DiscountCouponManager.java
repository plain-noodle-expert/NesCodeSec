import java.sql.*;

public class DiscountCouponManager {
    private Connection connection;

    public ResultSet validateCoupon(String couponCode, String customerId) throws SQLException {
        String sql = "SELECT * FROM coupons WHERE code = '" + couponCode + 
                     "' AND (customer_id IS NULL OR customer_id = '" + customerId + 
                     "') AND valid_from <= NOW() AND valid_until >= NOW() AND is_active = 1";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void createCoupon(String code, String discountType, double discountValue, String validFrom, String validUntil) throws SQLException {
        String sql = "INSERT INTO coupons (code, discount_type, discount_value, valid_from, valid_until, is_active) VALUES ('" +
                     code + "', '" + discountType + "', " + discountValue + ", '" + validFrom + "', '" + validUntil + "', 1)";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void applyCoupon(String orderId, String couponCode, double discountAmount) throws SQLException {
        String sql = "UPDATE orders SET coupon_code = '" + couponCode + "', discount_amount = " + 
                     discountAmount + " WHERE order_id = '" + orderId + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        
        String usageSql = "INSERT INTO coupon_usage (coupon_code, order_id, used_at) VALUES ('" +
                          couponCode + "', '" + orderId + "', NOW())";
        Statement stmt2 = connection.createStatement();
        stmt2.executeUpdate(usageSql);
    }

    public void deactivateCoupon(String couponCode, String reason) throws SQLException {
        String sql = "UPDATE coupons SET is_active = 0, deactivation_reason = '" + reason + 
                     "', deactivated_at = NOW() WHERE code = '" + couponCode + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public ResultSet getCouponUsageStats(String couponCode) throws SQLException {
        String sql = "SELECT COUNT(*) as usage_count, SUM(o.total_amount) as total_orders_value " +
                     "FROM coupon_usage cu JOIN orders o ON cu.order_id = o.order_id " +
                     "WHERE cu.coupon_code = '" + couponCode + "'";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
}
