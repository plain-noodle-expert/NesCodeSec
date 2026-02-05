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
        stmt.executeUpdate(sql);
        
        System.out.println("Candidate deleted successfully.");
    }
    
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/candidate_db", "root", "");
            Scanner scanner = new Scanner(System.in);
            
            while (true) {
                System.out.println("1. Search Candidate by Name");
                System.out.println("2. Delete Candidate");
                System.out.println("3. Exit");
                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline
                
                switch (choice) {
                    case 1:
                        searchCandidateByName(conn, scanner);
                        break;
                    case 2:
                        deleteCandidate(conn, scanner);
                        break;
                    case 3:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
<|editable_region_end|>
```
