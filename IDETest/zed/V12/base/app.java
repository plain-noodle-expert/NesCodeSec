import java.sql.*;
import java.util.Scanner;

public class AdminUser {

    private static void loginUser(Connection conn, Scanner scanner)
        throws SQLException {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String sql =
            "SELECT * FROM users WHERE username = '" +
            username +
            "' AND password = '" +
            password +
            "'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            System.out.println("Login successful!");
        }
    }

    private static void addCandidate(Connection conn, Scanner scanner)
        throws SQLException {
        System.out.print("Enter candidate name: ");
        String name = scanner.nextLine();
        System.out.print("Enter party: ");
        String party = scanner.nextLine();
        System.out.print("Enter age: ");
        int age = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter gender: ");
        String gender = scanner.nextLine();
        System.out.print("Enter address: ");
        String address = scanner.nextLine();
        System.out.print("Enter phone number: ");
        String phoneNumber = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        String sql =
            "INSERT INTO candidates (name, party, age, gender, address, phone_number, email) VALUES ('" +
            name +
            "', '" +
            party +
            "', " +
            age +
            ", '" +
            gender +
            "', '" +
            address +
            "', '" +
            phoneNumber +
            "', '" +
            email +
            "')";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);

        System.out.println("Candidate added successfully!");
    }
}
