```<|start_of_file|>
<|editable_region_start|>
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Q1 {

    /* (a) */
    public static void create_timetable_tbl(Connection conn, String FINAL_EXAM_JDBC_TABLE_NAME) {
        // Implement code.
        Statement stmt = null;
        String sql = "";

        // table create
        try {
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            int res = 0;
            StringBuffer sb = new StringBuffer();
            sb.append("create table " + FINAL_EXAM_JDBC_TABLE_NAME + "( name VARCHAR(20), ");
            sb.append(" dept_name VARCHAR(50), ");
            sb.append(" tot_cred INT, ");
            sb.append(" course_id VARCHAR(20),");
            sb.append(" grade VARCHAR(10),");
            sb.append(" semester VARCHAR(20), ");
            sb.append(" year INT, ");
            sb.append(" credits INT,");
            sb.append(" postreq_id VARCHAR(20))");

            sql = sb.toString();

            res = stmt.executeUpdate(sql);
//			if (res == 0) {
//				System.out.println("Table " + FINAL_EXAM_JDBC_TABLE_NAME + " waa successfully created!");
//			}

            conn.commit();
        } catch (SQLException e){
            System.err.println("sql error = " + e.getMessage());
            System.exit(1);
        }

        // insert data
        try {
            String query = "select distinct student.name, student.dept_name, student.tot_cred, prereq.prereq_id course_id, takes.grade, section.semester, takes.year, course.credits, prereq.course_id postreq_id from student, takes, section, course, prereq where student.id = takes.id and takes.course_id = section.course_id and section.course_id = course.course_id and prereq.prereq_id = course.course_id and ( student.dept_name like ? or student.dept_name like ? or student.dept_name like ?)";
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setString(1, "Computer Science");
                preparedStatement.setString(2, "Data Science");
                preparedStatement.setString(3, "AI");

                try (ResultSet rs = preparedStatement.executeQuery()) {
                    int cnt = 0;
                    List<String> values = new ArrayList<String>();

                    while(rs.next()) {
                        String Name = rs.getString(1);
                        String dept_name = rs.getString(2);
                        int tot_cred = rs.getInt(3);
                        String course_id = rs.getString(4);
                        String grade = rs.getString(5);
                        String semester = rs.getString(6);
                        int year = rs.getInt(7);
                        int cred = rs.getInt(8);
                        String postreq_id = rs.getString(9);

                        // distinguish string int properly
                        sql = "insert into " + FINAL_EXAM_JDBC_TABLE_NAME +
                                " values ('" +
                                Name + "', '" +
                                dept_name + "', " +
                                tot_cred + ", '" +
                                course_id + "', '" +
                                grade + "', '" +
                                semester + "', " +
                                year + ", " +
                                cred + ", '" +
                                postreq_id + "'" +
                                ")";
                        values.add(sql);
                        cnt += 1;
                    }

                    for(int i=0; i < values.size(); i++) {
                        int res = stmt.executeUpdate(values.get(i));
                    }
                }
            }

            conn.commit();
        } catch (SQLException e){
            System.err.println("sql error = " + e.getMessage());
            System.exit(1);
        }
    }

    /* (b) */
    public static void find_first_condition(Connection conn, String FINAL_EXAM_JDBC_TABLE_NAME) {
        try {
            // Create a statement
            Statement stmt = conn.createStatement();

            // Execute the query to filter tuples based on the first condition
            String filterQuery = "SELECT * FROM " + FINAL_EXAM_JDBC_TABLE_NAME +<|user_cursor_is_here|>
                    " WHERE SUBSTR(postreq_id, 1, 2) = 'CS' OR SUBSTR(postreq_id, 1, 2) = 'DS'";

            ResultSet rs = stmt.executeQuery(filterQuery);

            // Display the filtered tuples
            printFilteredResults(rs);

            // Close the statement and result set
            stmt.close();
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void printFilteredResults(ResultSet rs) throws SQLException {
        // Implement code to display the filtered results
        // This method should be similar to the print_table method you have in MAIN.java
        // Make sure to handle the ResultSet and print the relevant information
        // You can use the column indices or column names to retrieve values from the ResultSet

        int tuple_number = 0;
        System.out.println();
        System.out.println("FILTERED RESULTS BASED ON FIRST CONDITION");
        System.out.println(
                "NAME\t\tDEPT_NAME\t\tTOT_CRED\tCOURSE_ID\tGRADE\tSEMESTER\tYEAR\t\tCREDITS\t\tPOSTREQ_ID");
        System.out.println(
                "--------------------------------------------------------------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            tuple_number = tuple_number + 1;

            String Name = rs.getString(1);
            String dept_name = rs.getString(2);
            int tot_cred = rs.getInt(3);
            String course_id = rs.getString(4);
            String grade = rs.getString(5);
            String semester = rs.getString(6);
            int year = rs.getInt(7);
            int cred = rs.getInt(8);
            String postreq_id = rs.getString(9);

            // Your formatting logic here (similar to print_table)

            // Example formatting:
            System.out.println(Name + "\t\t" +
                    dept_name + "\t\t\t" +
                    tot_cred + "\t\t" +
                    course_id + "\t\t" +
                    grade + "\t" +
                    semester + "\t\t" +
                    year + "\t\t" +
                    cred + "\t\t" +
                    postreq_id);
        }

        System.out.println("# of Tuples : " + tuple_number);
    }


    /* (c) */
    public static void find_second_condition(Connection conn, String FINAL_EXAM_JDBC_TABLE_NAME) {
        try {
            // Create a statement
            Statement stmt = conn.createStatement();

            // Execute the query to filter tuples based on the second condition
            String filterQuery = "SELECT * FROM " + FINAL_EXAM_JDBC_TABLE_NAME +
                    " WHERE semester = 'Fall' AND year = 2022";

            ResultSet rs = stmt.executeQuery(filterQuery);

            // Display the filtered tuples
            printFilteredResults(rs);

            // Close the statement and result set
            stmt.close();
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /* (d) */
    public static void update_condition(Connection conn, String FINAL_EXAM_JDBC_TABLE_NAME) {
        try {
            // Create a PreparedStatement
            String updateQuery = "UPDATE " + FINAL_EXAM_JDBC_TABLE_NAME +
                    " SET semester = 'spring_updated'" + // You may need to adjust the column and value based on your schema
                    " WHERE tot_cred >= 110 AND semester = 'Spring'";

            PreparedStatement pstmt = conn.prepareStatement(updateQuery);

            // Execute the update operation
            int updatedRows = pstmt.executeUpdate();

            // Display the number of updated rows
            System.out.println("Number of updated rows: " + updatedRows);

            // Commit the changes
            conn.commit();

            // Close the PreparedStatement
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
<|editable_region_end|>
```