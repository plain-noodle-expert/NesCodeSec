<|editable_region_start|>
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class JavaJDBC {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/users", "root", "NEW_PASSWORD"
            );
            Statement statement = connection.createStatement();
            Scanner myObj = new Scanner(System.in);
            System.out.println("Enter password");
            String password_attempt = myObj.nextLine();
            ResultSet resultSet = statement.executeQuery("select * from employees where Password = " + "\'" + password_attempt + "\'");
            while (resultSet.next()) {
                int id = resultSet.getInt("user_id");
                String first_name = resultSet.getString("first_name");
                System.out.println("ID: " + id + " " + "first_name: " + first_name);
    
            }
            connection.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
<|editable_region_end|>
```
