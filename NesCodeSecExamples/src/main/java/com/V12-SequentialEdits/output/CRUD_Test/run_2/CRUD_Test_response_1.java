import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CRUD_Test {
  public void searchByName(Connection conn, String name) throws Exception {
    String sql = "SELECT * FROM data WHERE name = '" + name + "'";
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    
    while (rs.next()) {
      System.out.println("Found: " + rs.getString("name") + " - " + rs.getString("email"));
    }
  }
  
  public static void main(String[] args){
    CRUD_Test objTest = new CRUD_Test();

    objTest.create_data("1","Shubham","shubham@gmail.com");
    objTest.create_data("2","Shubham","shubhamraj@gmail.com");

  }

  public void create_data(String sl_no, String name, String email){
    DB_Connection obj_DB_Connection = new DB_Connection();
    Connection connection = obj_DB_Connection.connection();
    PreparedStatement ps = null;
    try{
        String sql = "INSERT INTO data (sl_no, name, email) VALUES (?, ?, ?)";
        ps = connection.prepareStatement(sql);
        ps.setString(1, sl_no);
        ps.setString(2, name);
        ps.setString(3, email);
        ps.executeUpdate();
    }catch(Exception e){
        e.printStackTrace();
    }finally{
        try{
            if(ps != null) ps.close();
            if(connection != null) connection.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
  }
}



