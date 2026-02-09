import java.sql.*;

public class DiscountCouponManager {
    private Connection connection;

    public ResultSet validateCoupon(String couponCode, String customerId) throws SQLException {
        String sql = "SELECT * FROM coupons WHERE code = ? AND (customer_id IS NULL OR customer_id = ?) AND valid_from <= NOW() AND valid_until >= NOW() AND is_active = 1";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, couponCode);
        pstmt.setString(2, customerId);
        return pstmt.executeQuery();
    }

    public void createCoupon(String code, String discountType, double discountValue, String validFrom, String validUntil) throws SQLException {
        String sql = "INSERT INTO coupons (code, discount_type, discount_value, valid_from, valid_until, is_active) VALUES (?, ?, ?, ?, ?, 1)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, code);
        pstmt.setString(2, discountType);
        pstmt.setDouble(3, discountValue);
        pstmt.setString(4, validFrom);
        pstmt.setString(5, validUntil);
        pstmt.executeUpdate();
    }

    public void applyCoupon(String orderId, String couponCode, double discountAmount) throws SQLException {
        String sql = "UPDATE orders SET coupon_code = ?, discount_amount = ? WHERE order_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, couponCode);
        pstmt.setDouble(2, discountAmount);
        pstmt.setString(3, orderId);
        pstmt.executeUpdate();
        
        String usageSql = "INSERT INTO coupon_usage (coupon_code, order_id, used_at) VALUES (?, ?, NOW())";
        PreparedStatement pstmt2 = connection.prepareStatement(usageSql);
        pstmt2.setString(1, couponCode);
        pstmt2.setString(2, orderId);
        pstmt2.executeUpdate();
    }

    public void deactivateCoupon(String couponCode, String reason) throws SQLException {
        String sql = "UPDATE coupons SET is_active = 0, deactivation_reason = ?, deactivated_at = NOW() WHERE code = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, reason);
        pstmt.setString(2, couponCode);
        pstmt.executeUpdate();
    }

    public ResultSet getCouponUsageStats(String couponCode) throws SQLException {
        String sql = "SELECT COUNT(*) as usage_count, SUM(o.total_amount) as total_orders_value " +
                     "FROM coupon_usage cu JOIN orders o ON cu.order_id = o.order_id " +
                     "WHERE cu.coupon_code = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, couponCode);
        return pstmt.executeQuery();
    }

    public void generateBulkCoupons(String prefix, int quantity, String discountType, double discountValue, String validFrom, String validUntil, String campaign) throws SQLException {
        String batchId = "BATCH_" + System.currentTimeMillis();
        
        for (int i = 1; i <= quantity; i++) {
            String code = prefix + "_" + String.format("%05d", i);
            String sql = "INSERT INTO coupons (code, discount_type, discount_value, valid_from, valid_until, is_active, batch_id, campaign) " +
                         "VALUES (?, ?, ?, ?, ?, 1, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, code);
            pstmt.setString(2, discountType);
            pstmt.setDouble(3, discountValue);
            pstmt.setString(4, validFrom);
            pstmt.setString(5, validUntil);
            pstmt.setString(6, batchId);
            pstmt.setString(7, campaign);
            pstmt.executeUpdate();
        }
        
        String logSql = "INSERT INTO coupon_generation_log (batch_id, prefix, quantity, campaign, generated_at) " +
                        "VALUES ('" + batchId + "', '" + prefix + "', " + quantity + ", '" + campaign + "', NOW())";
        PreparedStatement pstmt = connection.prepareStatement(logSql);
        pstmt.setString(1, batchId);
        pstmt.setString(2, prefix);
        pstmt.setInt(3, quantity);
        pstmt.setString(4, campaign);
        pstmt.executeUpdate();
    }

    public ResultSet getActiveCampaigns(String status) throws SQLException {
        String sql = "SELECT c.campaign, COUNT(DISTINCT c.code) as total_coupons, " +
                     "SUM(CASE WHEN cu.coupon_code IS NOT NULL THEN 1 ELSE 0 END) as used_coupons, " +
                     "SUM(CASE WHEN cu.coupon_code IS NOT NULL THEN 1 ELSE 0 END) / COUNT(DISTINCT c.code) * 100 as redemption_rate, " +
                     "SUM(o.discount_amount) as total_discount_given " +
                     "FROM coupons c " +
                     "LEFT JOIN coupon_usage cu ON c.code = cu.coupon_code " +
                     "LEFT JOIN orders o ON cu.order_id = o.order_id " +
                     "WHERE c.is_active = 1";
        
        if (status != null && !status.isEmpty()) {
            sql += " AND c.valid_until >= NOW()";
        }
        
        sql += " GROUP BY c.campaign ORDER BY redemption_rate DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        if (status != null && !status.isEmpty()) {
            sql += " AND c.valid_until >= NOW()";
        }
        sql += " GROUP BY c.campaign ORDER BY redemption_rate DESC";
        return pstmt.executeQuery();
    }

    public void setUsageLimit(String couponCode, int maxUsages, int maxUsagesPerCustomer) throws SQLException {
        String sql = "UPDATE coupons SET max_usages = ?, max_usages_per_customer = ? WHERE code = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, maxUsages);
        pstmt.setInt(2, maxUsagesPerCustomer);
        pstmt.setString(3, couponCode);
        pstmt.executeUpdate();
    }

    public ResultSet checkUsageEligibility(String couponCode, String customerId) throws SQLException {
        String sql = "SELECT c.code, c.max_usages, c.max_usages_per_customer, " +
                     "COUNT(cu.usage_id) as total_usages, " +
                     "SUM(CASE WHEN cu.customer_id = ? THEN 1 ELSE 0 END) as customer_usages " +
                     "FROM coupons c " +
                     "LEFT JOIN coupon_usage cu ON c.code = cu.coupon_code " +
                     "WHERE c.code = ? GROUP BY c.code";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        pstmt.setString(2, couponCode);
        return pstmt.executeQuery();
    }

    public void createDynamicCoupon(String customerId, String tierLevel, double discountValue, int validityDays) throws SQLException {
        String code = "DYNAMIC_" + customerId + "_" + System.currentTimeMillis();
        String sql = "INSERT INTO coupons (code, discount_type, discount_value, valid_from, valid_until, customer_id, is_active, coupon_source) " +
                     "VALUES (?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? DAY), ?, 1, 'DYNAMIC')";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, code);
        pstmt.setString(2, discountType);
        pstmt.setDouble(3, discountValue);
        pstmt.setInt(4, validityDays);
        pstmt.setString(5, customerId);
        pstmt.executeUpdate();
        
        String logSql = "INSERT INTO customer_rewards (customer_id, reward_type, reward_value, coupon_code, granted_at) " +
                        "VALUES (?, 'COUPON', ?, ?, NOW())";
        PreparedStatement logPstmt = connection.prepareStatement(logSql);
        logPstmt.setString(1, customerId);
        logPstmt.setDouble(2, discountValue);
        logPstmt.setString(3, code);
        logPstmt.executeUpdate();
    }

    public ResultSet analyzeCouponPerformance(String dateFrom, String dateTo, String campaign) throws SQLException {
        String sql = "SELECT c.campaign, c.discount_type, " +
                     "COUNT(DISTINCT cu.coupon_code) as unique_coupons_used, " +
                     "COUNT(cu.usage_id) as total_redemptions, " +
                     "SUM(o.total_amount) as gross_revenue, " +
                     "SUM(o.discount_amount) as total_discount_given, " +
                     "AVG(o.total_amount) as avg_order_value, " +
                     "SUM(o.total_amount) / SUM(o.discount_amount) as roi_ratio " +
                     "FROM coupons c " +
                     "JOIN coupon_usage cu ON c.code = cu.coupon_code " +
                     "JOIN orders o ON cu.order_id = o.order_id " +
                     "WHERE cu.used_at BETWEEN ? AND ?";
        
        if (campaign != null && !campaign.isEmpty()) {
            sql += " AND c.campaign = ?";
        }
        
        sql += " GROUP BY c.campaign, c.discount_type ORDER BY total_redemptions DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, dateFrom);
        pstmt.setString(2, dateTo);
        pstmt.setString(3, campaign != null && !campaign.isEmpty() ? campaign : null);
        return pstmt.executeQuery();
    }   

    public void extendCouponValidity(String couponCode, int additionalDays, String extendedBy) throws SQLException {
        String sql = "UPDATE coupons SET valid_until = DATE_ADD(valid_until, INTERVAL ? DAY), extended_by = ?, extended_at = NOW() WHERE code = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, additionalDays);
        pstmt.setString(2, extendedBy);
        pstmt.setString(3, couponCode);
        pstmt.executeUpdate();
        
        String logSql = "INSERT INTO coupon_modifications (coupon_code, modification_type, details, modified_by, timestamp) VALUES (?, 'EXTEND_VALIDITY', 'Extended by ? days', ?, NOW())";
        PreparedStatement logPstmt = connection.prepareStatement(logSql);
        logPstmt.setString(1, couponCode);
        logPstmt.setInt(2, additionalDays);
        logPstmt.setString(3, extendedBy);
        logPstmt.executeUpdate();
    }

    public ResultSet getExpiringSoonCoupons(int daysThreshold) throws SQLException {
        String sql = "SELECT c.*, COUNT(cu.usage_id) as times_used " +
                     "FROM coupons c " +
                     "LEFT JOIN coupon_usage cu ON c.code = cu.coupon_code " +
                     "WHERE c.is_active = 1 AND c.valid_until BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL ? DAY) " +
                     "GROUP BY c.code ORDER BY c.valid_until ASC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, daysThreshold);
        return pstmt.executeQuery();
    }
}
