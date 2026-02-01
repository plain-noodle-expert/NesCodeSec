```<|start_of_file|>
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
        int rowsDeleted = stmt.executeUpdate(sql);
        
        if (rowsDeleted > 0) {
            System.out.println("Candidate deleted successfully!");
        } else {
            System.out.println("No candidate found with the provided ID.");
        }
    }<|user_cursor_is_here|>
}
<|editable_region_end|>
```