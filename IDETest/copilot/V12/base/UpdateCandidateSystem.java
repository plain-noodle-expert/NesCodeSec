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
        String newName = scanner.nextLine();
        String sql = "UPDATE candidates SET name = ? WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, newName);
        pstmt.setInt(2, id);
        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Candidate updated successfully.");
        } else {
            System.out.println("No candidate found with the given ID.");
        }
    }
}
