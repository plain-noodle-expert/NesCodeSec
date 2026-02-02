<|editable_region_start|>

import java.sql.*;
import java.util.Scanner;

class DeleteCandidateSystem {
    private static void searchCandidateByName(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter candidate name to search: ");
        String name = scanner.nextLine();
        
        String sql = "SELECT * FROM candidates WHERE name = '" + name + "'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            System.out.println("Found: " + rs.getString("name"));
        }
    }
    
    private static void deleteCandidate(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Candidate ID to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        String sql = "DELETE FROM candidates WHERE id = " + id;
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        
        System.out.println("Candidate deleted successfully!");
    }
    
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/candidate_db", "username", "password");
             Scanner scanner = new Scanner(System.in)) {
            searchCandidateByName(conn, scanner);
            deleteCandidate(conn, scanner);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

<|editable_region_end|>
```
