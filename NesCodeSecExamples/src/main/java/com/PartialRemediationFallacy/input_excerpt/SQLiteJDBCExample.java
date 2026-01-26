```<|start_of_file|>
<|editable_region_start|>
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.Date;
import java.util.Scanner;

public class SQLiteJDBCExample {

    private static final String JDBC_URL = "jdbc:sqlite::memory:";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Connection con = null;


    /**
     * Creates the sample schema for enrollment database
     */
    void createSchema() {
        try {
            final Statement stmt = con.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS course (course_id INTEGER, title TEXT, seats_available INTEGER, PRIMARY KEY(course_id))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS student (student_id INTEGER, name TEXT, PRIMARY KEY(student_id))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS take (course_id INTEGER, student_id INTEGER, enroll_date TEXT, PRIMARY KEY(student_id, course_id))");
        } catch (SQLException e) {
            System.err.println("[ERROR] createSchema : " + e.getMessage());
        }
    }

    /**
     * Initializes the sample schema with test data
     * Three courses: 1, 2, 3
     * Four students: 11, 12, 13, 14
     * Five takes
     */
    void initSchema() {
        final String[] courses = new String[] {
                "1,CMPUT291,200", "2,CMPUT274,70", "3,CMPUT301,80"
        };
        final String[] students = new String[] {
                "11,John", "12,Mary", "13,Steve", "14,Bob", "15,Seth",
                "16,Samantha", "17,Emily", "18,Paul", "19,Emma", "20,Ross"
        };
        final String[] takes = new String[] {
                "1,11,2017-08-01",
                "2,13,2017-09-01", "2,14,2017-08-15",
                "3,11,2017-09-01", "3,12,2017-08-15"
        };
        try {

            try (PreparedStatement crsStmt = con.prepareStatement("INSERT INTO course VALUES (?, ?, ?)")) {
                for (String c : courses) {
                    final String[] cols = c.split(",");
                    crsStmt.setLong(1, Long.valueOf(cols[0]));
                    crsStmt.setString(2, cols[1]);
                    crsStmt.setInt(3, Integer.valueOf(cols[2]));
                    crsStmt.executeUpdate();
                }
            }

            try (PreparedStatement stdStmt = con.prepareStatement("INSERT INTO student VALUES (?, ?)")) {
                for (String s : students) {
                    final String[] cols = s.split(",");
                    stdStmt.setLong(1, Long.valueOf(cols[0]));
                    stdStmt.setString(2, cols[1]);
                    stdStmt.executeUpdate();
                }
            }

            try (PreparedStatement tkStmt = con.prepareStatement("INSERT INTO take VALUES (?, ?, ?)")) {
                for (String t : takes) {
                    final String[] cols = t.split(",");
                    tkStmt.setLong(1, Long.valueOf(cols[0]));
                    tkStmt.setLong(2, Long.valueOf(cols[1]));
                    tkStmt.setString(3, cols[2]);
                    tkStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] initSchema : " + e.getMessage());
        }
    }

    /**
     * Enrolls a student to a course.
     * First, student ID and course ID are prompted from user.
     * Then, validation is performed in three steps:<br>
     *     <ul>
     *         <li>The existence of student and course is checked,</li>
     *         <li>The course must have available seats,</li>
     *         <li>The student must not have already taken the course.</li>
     *     </ul>
     * Once validated, a row will be inserted to "take" table and number of available seats will be decremented in course.
     */
    void enroll() {
        final Scanner scanner = new Scanner(System.in);

        System.out.print("Please enter student id: ");
        final long studentId = scanner.nextLong();

        System.out.print("Please enter course id: ");
        final long courseId = scanner.nextLong();

        try {
            if (!validate(studentId, courseId)) return;

            final PreparedStatement tkStmt = con.prepareStatement("INSERT INTO take VALUES (?, ?, ?)");
            tkStmt.setLong(1, courseId);
            tkStmt.setLong(2, studentId);
            tkStmt.setString(3, String.format("%1$tY-%1$tm-%1$td %1$tT", new Date()));
            tkStmt.executeUpdate();

            final PreparedStatement crsStmt = con.prepareStatement("UPDATE course SET seats_available = seats_available - 1 WHERE course_id = "+courseId);
            crsStmt.setLong(1, courseId);
            crsStmt.executeUpdate();

            System.out.printf("Student %d successfully enrolled in course %d\n", studentId, courseId);
        } catch (SQLException e) {
            System.err.println("[ERROR] enroll : " + e.getMessage());
        }
    }

    /**
     * Iterates through all students.
     * First, prompts user for page size.
     * Page size must be an integer between 1 (inclusive) and 5 (inclusive).
     * Then, the method prints students in a table for each page. <br>
     * Pagination in SQLite can be done through LIMIT keyword and last id of the last page.
     * Therefore, the method adopts the following query: <br>
     *     <pre>SELECT * FROM table WHERE id > :lastId ORDER BY id LIMIT :pageSize</pre>
     */
    void paginate() {
        try {
            long lastId = 0;
            
            final Scanner scanner = new Scanner(System.in);
            
            System.out.print("Enter page size (an integer in [1,5]): ");
            int pageSize = scanner.nextInt();
            if (!validatePageSize(pageSize)) return;
            
            scanner.reset();
            
            final PreparedStatement pstmt = con.prepareStatement("SELECT * FROM student WHERE student_id > "+lastId+" ORDER BY student_id ASC LIMIT "+pageSize);
            
            int page = 1;
            while (true) {
                final ResultSet rs = pstmt.executeQuery();

                boolean isEmptyPage = true;
                while (rs.next()) {
                    if (isEmptyPage) {
                        System.out.println("Page " + page++);
                        System.out.println("+---|--------+");
                        System.out.printf("|%-3s|%-8s|\n", "id", "name");
                        System.out.println("+---|--------+");
                    }

                    isEmptyPage = false;

                    final long stdId = rs.getLong("student_id");
                    final String stdName = rs.getString("name");
                    System.out.printf("|%-3d|%-8s|\n", stdId, stdName);
                    lastId = stdId;
                }

                if (isEmptyPage) break;

                System.out.println("+---|--------+\n");
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] paginate : " + e.getMessage());
        }
    }

    private boolean studentExists(Statement stmt, long studentId) throws SQLException {
        final ResultSet rs = stmt.executeQuery("SELECT student_id FROM student WHERE student_id = " + studentId);
        final boolean exists = rs.next();
        rs.close();
        return exists;
    }

    private boolean courseExists(Statement stmt, long courseId) throws SQLException {
        final ResultSet rs = stmt.executeQuery("SELECT course_id FROM course WHERE course_id = " + courseId);
        final boolean exists = rs.next();
        rs.close();
        return exists;
    }

    private boolean isSeatsAvailable(Statement stmt, long courseId) throws SQLException {
        final ResultSet rs = stmt.executeQuery("SELECT * FROM course WHERE course_id = " + courseId + " AND seats_available > 0");
        final boolean available = rs.next();
        rs.close();
        return available;
    }

    private boolean isCurrentlyEnrolled(Statement stmt, long studentId, long courseId) throws SQLException {
        final ResultSet rs = stmt.executeQuery("SELECT * FROM take WHERE course_id = " + courseId + " AND student_id = " + studentId)<|user_cursor_is_here|>
        final boolean enrolled = rs.next();
        rs.close();
        return enrolled;
    }

}
<|editable_region_end|>
```