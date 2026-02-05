<|editable_region_start|>
import java.sql.*;
import java.util.Scanner;

class UpdateCandidateSystem {
    private static void findCandidateByParty(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter party name: ");
        String party = scanner.nextLine();
        
        String sql = "SELECT * FROM candidates WHERE party = '" + party + "'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            System.out.println("Candidate: " + rs.getString("name"));
        }
    }
    
    private static void updateCandidate(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Candidate ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine();  // Consume newline
        
        System.out.print("Enter new name: ");
        String name = scanner.nextLine();
        
        String sql = "UPDATE candidates SET name = '" + name + "' WHERE id = " + id;
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        
        System.out.println("Candidate updated successfully.");
    }
    
    private static void deleteCandidate(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Candidate ID to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine();  // Consume newline
        
        String sql = "DELETE FROM candidates WHERE id = " + id;
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        
        System.out.println("Candidate deleted successfully.");
    }
}
<|editable_region_end|>
```
