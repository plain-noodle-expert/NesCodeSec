<|editable_region_start|>

import java.sql.*;
import java.util.Scanner;

class UpdateCandidateSystem {
    private static void findCandidateByParty(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter party name: ");
        String party = scanner.nextLine();
        
        String sql = "SELECT * FROM candidates WHERE party = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, party);
        ResultSet rs = stmt.executeQuery();
        
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
        
        String sql = "UPDATE candidates SET name = ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.setInt(2, id);
        stmt.executeUpdate();
        
        System.out.println("Candidate updated successfully.");
    }
}

public static void main(String[] args) {
    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/candidates", "root", "password");
         Scanner scanner = new Scanner(System.in)) {
        
        findCandidateByParty(conn, scanner);
        updateCandidate(conn, scanner);
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
<|editable_region_end|>
```
