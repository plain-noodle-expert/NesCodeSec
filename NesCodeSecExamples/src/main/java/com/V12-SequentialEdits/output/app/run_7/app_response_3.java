<|editable_region_start|>

import java.sql.*;
import java.util.Scanner;

public class AdminUser {
    private static void loginUser(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            System.out.println("Login successful!");
        }
    }
    
    private static void addCandidate(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter candidate name: ");
        String name = scanner.nextLine();
        System.out.print("Enter party: ");
        String party = scanner.nextLine();
        
        String sql = "INSERT INTO candidates (name, party) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.setString(2, party);
        stmt.executeUpdate();
        
        System.out.println("Candidate added successfully!");
    }
}



public static void main(String[] args) {
    try {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/election", "root", "password");
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Welcome to the Election Admin System!");
        System.out.println("1. Login");
        System.out.println("2. Add Candidate");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                loginUser(conn, scanner);
                break;
            case 2:
                addCandidate(conn, scanner);
                break;
            default:
                System.out.println("Invalid choice!");
        }
        
        scanner.close();
    } catch (SQLException e) {
        System.out.println("Error connecting to database!");
        e.printStackTrace();
    }
}

public static void main(String[] args) {
    try {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/election", "root", "password");
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Welcome to the Election Admin System!");
        System.out.println("1. Login");
        System.out.println("2. Add Candidate");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                loginUser(conn, scanner);
                break;
            case 2:
                addCandidate(conn, scanner);
                break;
            default:
                System.out.println("Invalid choice!");
        }
        
        scanner.close();
    } catch (SQLException e) {
        System.out.println("Error connecting to database!");
        e.printStackTrace();
    }
}

<|editable_region_end|>
```
