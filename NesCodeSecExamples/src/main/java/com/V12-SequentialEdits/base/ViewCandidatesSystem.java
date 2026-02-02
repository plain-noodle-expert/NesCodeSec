import java.sql.*;
import java.util.Scanner;

class ViewCandidatesSystem {
    private static void filterCandidatesByVotes(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter minimum vote count: ");
        String minVotes = scanner.nextLine();
        
        String query = "SELECT * FROM candidates WHERE votecount > " + minVotes + " ORDER BY votecount DESC";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        while (rs.next()) {
            System.out.println(rs.getString("name") + ": " + rs.getInt("votecount") + " votes");
        }
    }
    
    private static void viewCandidates(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n====== View Candidate ======");
        System.out.print("Enter Candidate ID to view: ");
        
    }
}
