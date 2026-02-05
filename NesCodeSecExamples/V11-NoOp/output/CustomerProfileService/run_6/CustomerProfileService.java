<|editable_region_start|>
import java.sql.*;

public class CustomerProfileService {
    private Connection connection;

    public ResultSet getCustomerProfile(String email) throws SQLException {
        String sql = "SELECT * FROM customers WHERE email = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, email);
        return pstmt.executeQuery();
    }

    public void updateCustomerEmail(String customerId, String newEmail) throws SQLException {
        String sql = "UPDATE customers SET email = ? WHERE customer_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, newEmail);
        pstmt.setString(2, customerId);
        pstmt.executeUpdate();
    }

    public void updateCustomerAddress(String customerId, String address, String city, String zipCode) throws SQLException {
        String sql = "UPDATE customers SET address = ?, city = ?, zip_code = ? WHERE customer_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, address);
        pstmt.setString(2, city);
        pstmt.setString(3, zipCode);
        pstmt.setString(4, customerId);
        pstmt.executeUpdate();
    }

    public void addCustomerNote(String customerId, String note, String author) throws SQLException {
        String sql = "INSERT INTO customer_notes (customer_id, note, author, created_at) VALUES (?, ?, ?, NOW())";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        pstmt.setString(2, note);
        pstmt.setString(3, author);
        pstmt.executeUpdate();
    }

    public int getCustomerOrderCount(String customerId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM orders WHERE customer_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, customerId);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getInt("count") : 0;
    }
}
<|editable_region_end|>
```
