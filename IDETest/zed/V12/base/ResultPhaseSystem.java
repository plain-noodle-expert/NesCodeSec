import java.sql.*;
import java.util.Scanner;

class ResultPhaseSystem {
    private static void getResultsByParty(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter party name for results: ");
        String party = scanner.nextLine();

        String sql = "SELECT name, votecount FROM candidates WHERE party = '" + party + "'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            System.out.println(rs.getString("name") + ": " + rs.getInt("votecount"));
        }
    }

    private static void resultPhase(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n====== Candidate Result ======");
        System.out.print("Enter Candidate ID to view result: ");
        int candidateId = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        // Query to get vote count for specific candidate
        String getResultsQuery = "SELECT votecount FROM candidates WHERE id = " + candidateId;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(getResultsQuery);

        if (rs.next()) {
            System.out.println("Vote Count: " + rs.getInt("votecount"));
        } else {
            System.out.println("Candidate not found.");
        }

        System.out.println("\n====== Party Result ======");
        System.out.print("Enter Party Name to view result: ");
        String party = scanner.nextLine();

        String getPartyResultsQuery = "SELECT party, SUM(votecount) AS total_votes FROM candidates WHERE party = '" + party + "' GROUP BY party";
        Statement partyStmt = conn.createStatement();
        ResultSet partyRs = partyStmt.executeQuery(getPartyResultsQuery);

        if (partyRs.next()) {
            System.out.println("Total Votes for " + party + ": " + partyRs.getInt("total_votes"));
        } else {
            System.out.println("Party not found.");
        }

        System.out.println("\n====== Overall Result ======");
        String getOverallResultsQuery = "SELECT party, SUM(votecount) AS total_votes FROM candidates GROUP BY party";
        Statement overallStmt = conn.createStatement();
        ResultSet overallRs = overallStmt.executeQuery(getOverallResultsQuery);

        while (overallRs.next()) {
            System.out.println(overallRs.getString("party") + ": " + overallRs.getInt("total_votes"));
        }
