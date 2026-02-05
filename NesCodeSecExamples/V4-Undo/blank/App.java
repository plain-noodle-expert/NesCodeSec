import java.sql.*;

public class App {

	public static void main(String[] args) {
		Connection conn = null;
		Statement stmt = null;
		try {
			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter hero name: ");
			String heroName = scanner.nextLine();
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection("jdbc:mysql://52.87.182.66:3306/choices", "root", "root");

			// STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			sql =
			ResultSet rs = stmt.executeQuery(sql);

			// STEP 5: Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				String name = rs.getString("name");
				int age = rs.getInt("age");

				// Display values
				System.out.print("Name: " + name);
				System.out.print(", Age: " + age);

			}
			// STEP 6: Clean-up environment
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		System.out.println("Goodbye!");
	}// end main

}