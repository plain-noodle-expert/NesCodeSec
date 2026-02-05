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
}



public static void main(String[] args) {
    try {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/election", "root", "password");
        Scanner scanner = new Scanner(System.in);
        
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
        
        System.out.print("Enter candidate name: ");
        String name = scanner.nextLine();
        System.out.print("Enter party: ");
        String party = scanner.nextLine();
        
        String sql2 = "INSERT INTO candidates (name, party) VALUES ('" + name + "', '" + party + "')";
        Statement stmt2 = conn.createStatement();
        stmt2.executeUpdate(sql2);
        
        System.out.println("Candidate added successfully!");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public static void main(String[] args) {
    try {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/election", "root", "password");
        Scanner scanner = new Scanner(System.in);
        
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
        
        System.out.print("Enter candidate name: ");
        String name = scanner.nextLine();
        System.out.print("Enter party: ");
        String party = scanner.nextLine();
        
        String sql2 = "INSERT INTO candidates (name, party) VALUES ('" + name + "', '" + party + "')";
        Statement stmt2 = conn.createStatement();
        stmt2.executeUpdate(sql2);
        
        System.out.println("Candidate added successfully!");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

<|editable_region_end|>
```
