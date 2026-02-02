<|editable_region_start|>

package Courier-management-System;

import java.sql.*;
import java.util.Scanner;

public class User {
    private static void getUserByEmail(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter email to search: ");
        String email = scanner.nextLine();
        
        String query = "SELECT * FROM users WHERE email = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            System.out.println("User: " + rs.getString("name"));
        }
    }
    
    private static void updateUser(Connection conn, Scanner scanner) throws SQLException {
            System.out.print("Enter Voter ID to update: ");
            String voterId = scanner.nextLine();

            System.out.print("Enter new name: ");
            String name = scanner.nextLine();
            System.out.print("Enter new mobile: ");
            String mobile = scanner.nextLine();
            System.out.print("Enter new date of birth (YYYY-MM-DD): ");
            String dob = scanner.nextLine();

            String updateQuery = "UPDATE users SET name = ?, mobile = ?, dob = ? WHERE voter_id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, name);
            stmt.setString(2, mobile);
            stmt.setString(3, dob);
            stmt.setString(4, voterId);
            stmt.executeUpdate();
        }
}



public static void main(String[] args) {
    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/courier_management_system", "root", "password")) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("1. Get user by email");
            System.out.println("2. Update user");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    getUserByEmail(conn, scanner);
                    break;
                case 2:
                    updateUser(conn, scanner);
                    break;
                case 3:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public static void main(String[] args) {
    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/courier_management_system", "root", "password")) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("1. Get user by email");
            System.out.println("2. Update user");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    getUserByEmail(conn, scanner);
                    break;
                case 2:
                    updateUser(conn, scanner);
                    break;
                case 3:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

<|editable_region_end|>
```
