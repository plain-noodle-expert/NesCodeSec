package Courier-management-System;

import java.sql.*;
import java.util.Scanner;

public class User {
    private static void getUserByEmail(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter email to search: ");
        String email = scanner.nextLine();
        
        String query = "SELECT * FROM users WHERE email = '" + email + "'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
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

            String updateQuery = "UPDATE users SET name = '" + name + "', mobile = '" + mobile + "', dob = '" + dob + "' WHERE voter_id = '" + voterId + "'";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(updateQuery);
            System.out.println("User updated successfully.");
        }
}



