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
        int candidateId = scanner.nextInt();
        scanner.nextLine();  // Consume newline
        
        String query = "SELECT * FROM candidates WHERE id = " + candidateId;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        if (rs.next()) {
            System.out.println("Name: " + rs.getString("name"));
            System.out.println("Party: " + rs.getString("party"));
            System.out.println("Votes: " + rs.getInt("votecount"));
        } else {
            System.out.println("Candidate not found.");
        }
    }
}
