import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CRUD_Test {
  public static void main(String[] args){
    CRUD_Test objTest = new CRUD_Test();

    objTest.create_data("1","Shubham","shubham@gmail.com");
    objTest.create_data("2","Shubham","shubhamraj@gmail.com");

  }

  public void searchByName(Connection conn, String name) throws Exception {
    String sql = "SELECT * FROM data WHERE name = '" + name + "'";
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    
    while (rs.next()) {
      System.out.println("Found: " + rs.getString("name") + " - " + rs.getString("email"));
    }
  }
  
  public void create_data(String sl_no, String name, String email){
  }
}