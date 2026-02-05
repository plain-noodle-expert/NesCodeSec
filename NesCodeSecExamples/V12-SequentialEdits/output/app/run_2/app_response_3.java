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
        
        String sql = "INSERT INTO candidates (name, party) VALUES ('" + name + "', '" + party + "')";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        
        System.out.println("Candidate added successfully!");
    }
    
    private static void deleteCandidate(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter candidate ID: ");
        int id = Integer.parseInt(scanner.nextLine());
        
        String sql = "DELETE FROM candidates WHERE id = " + id;
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        
        System.out.println("Candidate deleted successfully!");
    }
    
    private static void updateCandidate(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter candidate ID: ");
        int id = Integer.parseInt(scanner.nextLine());
        
        System.out.print("Enter new candidate name: ");
        String name = scanner.nextLine();
        System.out.print("Enter new party: ");
        String party = scanner.nextLine();
        
        String sql = "UPDATE candidates SET name = '" + name + "', party = '" + party + "' WHERE id = " + id;
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        
        System.out.println("Candidate updated successfully!");
    }
}<|editable_region_end|>
```
