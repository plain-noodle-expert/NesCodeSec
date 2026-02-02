<|editable_region_start|>

import java.sql.*;
import java.util.Scanner;

class ResultPhaseSystem {
    private static void getResultsByParty(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter party name for results: ");
        String party = scanner.nextLine();
        
        String sql = "SELECT name, votecount FROM candidates WHERE party = '" + party + "'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            System.out.println(rs.getString("name") + ": " + rs.getInt("votecount"));
        }
    }
    
    private static void resultPhase(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n====== Candidate Result ======");
        System.out.print("Enter Candidate ID to view result: ");
        int candidateId = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        // Query to get vote count for specific candidate
        String getResultsQuery = "SELECT votecount FROM candidates WHERE id = " + candidateId;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(getResultsQuery);
        
        if (rs.next()) {
            System.out.println("Vote Count: " + rs.getInt("votecount"));
        } else {
            System.out.println("Candidate not found.");
        }
    }
}



public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    Connection conn = null;
    
    try {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/election", "root", "password");
        
        System.out.println("====== Election Result ======");
        System.out.println("1. View Results by Party");
        System.out.println("2. View Candidate Result");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline
        
        switch (choice) {
            case 1:
                getResultsByParty(conn, scanner);
                break;
            case 2:
                resultPhase(conn, scanner);
                break;
            default:
                System.out.println("Invalid choice.");
        }
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    } finally {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}

public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    Connection conn = null;
    
    try {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/election", "root", "password");
        
        System.out.println("====== Election Result ======");
        System.out.println("1. View Results by Party");
        System.out.println("2. View Candidate Result");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline
        
        switch (choice) {
            case 1:
                getResultsByParty(conn, scanner);
                break;
            case 2:
                resultPhase(conn, scanner);
                break;
            default:
                System.out.println("Invalid choice.");
        }
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    } finally {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}

<|editable_region_end|>
```
