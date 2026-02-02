import java.sql.*;

public class CustomerProfileService {
    private Connection connection;

    public ResultSet getCustomerProfile(String email) throws SQLException {
        String sql = "SELECT * FROM customers WHERE email = '" + email + "'";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void updateCustomerEmail(String customerId, String newEmail) throws SQLException {
        String sql = "UPDATE customers SET email = '" + newEmail + "' WHERE customer_id = '" + customerId + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void updateCustomerAddress(String customerId, String address, String city, String zipCode) throws SQLException {
        String sql = "UPDATE customers SET address = '" + address + "', city = '" + city + 
                     "', zip_code = '" + zipCode + "' WHERE customer_id = '" + customerId + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void addCustomerNote(String customerId, String note, String author) throws SQLException {
        String sql = "INSERT INTO customer_notes (customer_id, note, author, created_at) VALUES ('" +
                     customerId + "', '" + note + "', '" + author + "', NOW())";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public int getCustomerOrderCount(String customerId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM orders WHERE customer_id = '" + customerId + "'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        return rs.next() ? rs.getInt("count") : 0;
    }
}
