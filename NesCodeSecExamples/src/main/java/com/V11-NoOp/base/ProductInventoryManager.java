import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductInventoryManager {
    private Connection connection;

    public List<String> searchProducts(String category, String keyword) throws SQLException {
        String sql = "SELECT * FROM products WHERE category = '" + category + "' AND name LIKE '%" + keyword + "%'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        List<String> products = new ArrayList<>();
        while (rs.next()) {
            products.add(rs.getString("name"));
        }
        return products;
    }

    public void updateStock(String productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock = stock + " + quantity + " WHERE product_id = '" + productId + "'";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void addProduct(String productName, String category, double price, int stock) throws SQLException {
        String sql = "INSERT INTO products (name, category, price, stock) VALUES ('" + 
                     productName + "', '" + category + "', " + price + ", " + stock + ")";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public double calculateTotalValue(String category) throws SQLException {
        String sql = "SELECT SUM(price * stock) as total FROM products WHERE category = '" + category + "'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        return rs.next() ? rs.getDouble("total") : 0.0;
    }
}
