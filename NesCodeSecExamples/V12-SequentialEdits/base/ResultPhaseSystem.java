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
        
    }
}
