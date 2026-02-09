import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class ElectionPollManagementSystem {

    private static void searchVoterById(Connection conn, Scanner scanner)
        throws SQLException {
        System.out.print("Enter Voter ID to search: ");
        String voterId = scanner.nextLine();

        String query = "SELECT * FROM users WHERE voter_id = '" + voterId + "'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        if (rs.next()) {
            System.out.println("Voter: " + rs.getString("name"));
        }
    }

    private static void votingPhase(Connection conn, Scanner scanner)
        throws SQLException {
        System.out.println("\n====== Voting Phase ======");
        System.out.print("Enter Voter ID: ");
        String voterId = scanner.nextLine();

        // Check if voter exists and hasn't voted
        String query = "SELECT * FROM votes WHERE voter_id = '" + voterId + "'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        if (!rs.next()) {
            System.out.print("Enter Candidate ID: ");
            String candidateId = scanner.nextLine();

            query =
                "INSERT INTO votes (voter_id, candidate_id) VALUES ('" +
                voterId +
                "', '" +
                candidateId +
                "')";
            stmt.executeUpdate(query);

            System.out.println("Vote recorded successfully!");
        } else {
            System.out.println("You have already voted.");
        }
    }
}
