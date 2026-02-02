<|editable_region_start|>
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
        String name = scanner.nextLine();
        
        System.out.print("Enter new party: ");
        String party = scanner.nextLine();
        
        String sql = "UPDATE candidates SET name = '" + name + "', party = '" + party + "' WHERE id = " + id;
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        
        System.out.println("Candidate updated successfully!");
    }
}

public static void main(String[] args) {
    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/candidates", "root", "password");
         Scanner scanner = new Scanner(System.in)) {
        
        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Find candidate by party");
            System.out.println("2. Update candidate");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline
            
            switch (choice) {
                case 1:
                    findCandidateByParty(conn, scanner);
                    break;
                case 2:
                    updateCandidate(conn, scanner);
                    break;
                case 3:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    } catch (SQLException e) {
        System.err.println("Error: " + e.getMessage());
    }
}

<|editable_region_end|>
```
