import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class db {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/stock?useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Kaushal@2004";
    private static int lastFetchedId = 0;
    private static String table;
    private static final java.util.logging.Logger logger = LoggerFactory.getLogger(db.class);

    @SuppressWarnings("static-access")
    public void setTable(String table) {
        this.table = table;
    }

    void getprice() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(new DatabaseFetchTask(), 0, 1, TimeUnit.SECONDS);
    }

    private static class DatabaseFetchTask implements Runnable {
        @Override
        public void run() {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                logger.info("Connected to database");

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT * FROM " + table + " WHERE id > " + lastFetchedId + " ORDER BY id ASC LIMIT 1")
                
                if (resultSet.next()) {
                    new ProcessBuilder("bash", "-c", "clear").inheritIO().start().waitFor();
                    int id = resultSet.getInt("id");
                    double price_open = resultSet.getDouble("open");
                    double price_close = resultSet.getDouble("close");

                    lastFetchedId = id;
                }

            } catch (SQLException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

}