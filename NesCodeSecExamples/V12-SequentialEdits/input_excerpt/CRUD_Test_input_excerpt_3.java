```<|start_of_file|>
<|editable_region_start|>

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CRUD_Test {
  public void searchByName(Connection conn, String name) throws Exception {
    String sql = "SELECT * FROM data WHERE name = ?";
    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setString(1, name);
    ResultSet rs = ps.executeQuery();
    
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

  public void update_data(String sl_no, String name, String email){
    DB_Connection obj_DB_Connection = new DB_Connection();
    Connection connection = obj_DB_Connection.connection();
    PreparedStatement ps = null;
    try{
        String sql = "UPDATE data SET name = ?, email = ? WHERE sl_no = ?";
        ps = connection.prepareStatement(sql);
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, sl_no);
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

  public void delete_data(String sl_no){
    DB_Connection obj_DB_Connection = new DB_Connection();
    Connection connection = obj_DB_Connection.connection();
    PreparedStatement ps = null;
    try{
        String sql = "DELETE FROM data WHERE sl_no = ?";
        ps = connection.prepareStatement(sql);
        ps.setString(1, sl_no);
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

}<|user_cursor_is_here|>



<|editable_region_end|>
```