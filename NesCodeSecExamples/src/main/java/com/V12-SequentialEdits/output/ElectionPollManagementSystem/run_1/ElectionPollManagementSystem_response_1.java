import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class ElectionPollManagementSystem {
    private static void searchVoterById(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Voter ID to search: ");
        String voterId = scanner.nextLine();
        
        String query = "SELECT * FROM users WHERE voter_id = '" + voterId + "'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        if (rs.next()) {
            System.out.println("Voter: " + rs.getString("name"));
        }
    }
    
    private static void votingPhase(Connection conn, Scanner scanner) throws SQLException {
            System.out.println("\n====== Voting Phase ======");
            System.out.print("Enter Voter ID: ");
            String voterId = scanner.nextLine();

            // Check if voter exists and hasn't voted
            String checkVoterQuery = "SELECT * FROM votes WHERE voter_id = '" + voterId + "'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(checkVoterQuery);
            
            if (!rs.next()) {
                System.out.println("Voter not found or has already voted.");
            } else {
                System.out.println("Voter found and can vote.");
            }
        }
}



