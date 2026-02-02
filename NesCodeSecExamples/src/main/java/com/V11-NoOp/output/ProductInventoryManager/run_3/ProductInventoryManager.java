<|editable_region_start|>
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductInventoryManager {
    private Connection connection;

    public List<String> searchProducts(String category, String keyword) throws SQLException {
        String sql = "SELECT * FROM products WHERE category = ? AND name LIKE ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, category);
        pstmt.setString(2, "%" + keyword + "%");
        ResultSet rs = pstmt.executeQuery();
        
        List<String> products = new ArrayList<>();
        while (rs.next()) {
            products.add(rs.getString("name"));
        }
        return products;
    }

    public void updateStock(String productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock = stock + ? WHERE product_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, quantity);
        pstmt.setString(2, productId);
        pstmt.executeUpdate();
    }

    public void addProduct(String productName, String category, double price, int stock) throws SQLException {
        String sql = "INSERT INTO products (name, category, price, stock) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, productName);
        pstmt.setString(2, category);
        pstmt.setDouble(3, price);
        pstmt.setInt(4, stock);
        pstmt.executeUpdate();
    }

    public double calculateTotalValue(String category) throws SQLException {
        String sql = "SELECT SUM(price * stock) as total FROM products WHERE category = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, category);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getDouble("total") : 0.0;
    }
}
<|editable_region_end|>
```
