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
        
    }
}
