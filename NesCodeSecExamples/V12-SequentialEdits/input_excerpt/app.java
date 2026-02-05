```<|start_of_file|>
<|editable_region_start|>
import java.sql.*;
import java.util.Scanner;

public class AdminUser {
    private static void loginUser(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        String sql = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        if (rs.next()) {
            System.out.println("Login successful!");
        }
    }
    
    private static void addCandidate(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter candidate name: ");
        String name = scanner.nextLine();
        System.out.print("Enter party: ");
        String party = scanner.nextLine();
        <|user_cursor_is_here|>
    }
}
<|editable_region_end|>
```