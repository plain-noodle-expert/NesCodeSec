<|editable_region_start|>

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

        }
}



public class UserResponse1 {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/courier_management_system", "root", "password");
            Scanner scanner = new Scanner(System.in);
            
            getUserByEmail(conn, scanner);
            updateUser(conn, scanner);
            
            scanner.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

<|editable_region_end|>
```
