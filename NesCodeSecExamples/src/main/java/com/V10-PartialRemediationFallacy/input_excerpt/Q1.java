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
                    
                    // Prepare the insert statement once
                    sql = "insert into " + FINAL_EXAM_JDBC_TABLE_NAME +
                            " values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(sql);

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

                        // Use parameterized query to prevent SQL injection
                        insertStmt.setString(1, Name);
                        insertStmt.setString(2, dept_name);
                        insertStmt.setInt(3, tot_cred);
                        insertStmt.setString(4, course_id);
                        insertStmt.setString(5, grade);
                        insertStmt.setString(6, semester);
                        insertStmt.setInt(7, year);
                        insertStmt.setInt(8, cred);
                        insertStmt.setString(9, postreq_id);
                        
                        insertStmt.executeUpdate();
                        cnt += 1;
                    }
                    
                    insertStmt.close();
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
            String filterQuery = "SELECT * FROM " + FINAL_EXAM_JDBC_TABLE_NAME +
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

    /* (c) */
    public static void find_second_condition(Connection conn, String FINAL_EXAM_JDBC_TABLE_NAME) {
        try {
            // Create a statement
            Statement stmt = conn.createStatement();

            // Execute the query to filter tuples based on the second condition
            String filterQuery = "SELECT * FROM " + FINAL_EXAM_JDBC_TABLE_NAME +
                    " WHERE semester = 'Fall' AND year = 2022"<|user_cursor_is_here|>

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
        String tableName = validateTableName(FINAL_EXAM_JDBC_TABLE_NAME);
        String updateSql = "UPDATE " + tableName + " SET semester = ? WHERE tot_cred >= ? AND semester = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setString(1, "spring_updated");
            pstmt.setInt(2, 110);
            pstmt.setString(3, "Spring");
            int updatedRows = pstmt.executeUpdate();
            conn.commit();
            System.out.println("Number of updated rows: " + updatedRows);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
<|editable_region_end|>
```