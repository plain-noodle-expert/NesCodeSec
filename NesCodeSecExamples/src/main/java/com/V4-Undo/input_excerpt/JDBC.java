```<|start_of_file|>
<|editable_region_start|>
import java.io.IOException; 
import java.sql.Connection;
import java.sql.DriverManager; 
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.Date;

public class JDBC {
	public static String getOrganizerEventData(int ID) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
				e.printStackTrace();
		}
		
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		
		String json = "";
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/ticketmaster?user=root&password=root");
			st = conn.createStatement();
			rs = st.executeQuery<|user_cursor_is_here|>
			while(rs.next()) {
					String name = rs.getString("name");
					int organizer_id = rs.getInt("organizer_id");
					Date event_date = rs.getDate("event_date");
					Time event_time = rs.getTime("event_time");
					String city = rs.getString("city");
					String state = rs.getString("state");
					int total_availability = rs.getInt("total_availability");
					int availability = rs.getInt("availability");
					int price = rs.getInt("price");
					String description = rs.getString("description");
					String img_file = rs.getString("img_file");

					json += "{\"name\"" + ": \"" + name + "\"," + 
							"\"organizer_id\"" + ": \"" + organizer_id + "\"," + 
							"\"event_date\"" + ": \"" + event_date + "\"," +
							"\"event_time\"" + ": \"" + event_time + "\"," +
							"\"city\"" + ": \"" + city + "\"," +
							"\"state\"" + ": \"" + state + "\"," +
							"\"total_availability\"" + ": \"" + total_availability + "\"," +
							"\"availability\"" + ": \"" + availability + "\"," +
							"\"price\"" + ": \"" + price + "\"," +
							"\"description\"" + ": \"" + description + "\"," +
							"\"img_file\"" + ": \"" + img_file + "\"}*";
			}
		} catch(SQLException sqle) {
			sqle.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqle) {
				System.out.println("sqle: " + sqle.getMessage());
			}
		}
		if (json.length() > 1) {
			return json.substring(0, json.length()-1);
		} else {
			return "";
		}
	}
	
	public static int buyTicket(String eventName, int userID, int num) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
				e.printStackTrace();
		}
		
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		
		String json = "";
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/ticketmaster?user=root&password=root");
			st = conn.createStatement();
			rs = st.executeQuery("SELECT * FROM events WHERE name='"+eventName+"'");
			if (rs.next()) {
				int event_id = rs.getInt("id");
				int organizer_id = rs.getInt("organizer_id");
				int availability = rs.getInt("availability");
				int price = rs.getInt("price");
				
				if (availability >= num) {
					rs = st.executeQuery("SELECT * FROM users WHERE id='"+userID+"'");
					if (rs.next()){
						double balance = rs.getDouble("balance");
						if (balance < num*price) {
							return -1;
						}
						rs = st.executeQuery("SELECT * FROM organizers WHERE id='"+organizer_id+"'");
						
						if (rs.next()) {
							double org_balance = rs.getDouble("balance");
							
							st.execute("UPDATE events SET availability=" + (availability - num) + " where name='"+ eventName + "'");
							st.execute("UPDATE users SET balance=" + (balance - num*price) + " where id='"+ userID + "'");						
							st.execute("UPDATE organizers SET balance=" + (org_balance + num*price) + " where id='"+ userID + "'");
							
							rs = st.executeQuery("SELECT * FROM userevents WHERE user_id='"+userID+"' AND id='"+event_id+"'");
							if (rs.next()) {
								int quantity = rs.getInt("quantity");
								st.execute("UPDATE userevents SET quantity=" + (quantity + num) + " where user_id='"+ userID + "' AND id='"+event_id+"'");
							}
							else {
								st.execute("INSERT INTO userevents (id, user_id, quantity) VALUES (" + event_id + ", " + userID + ", " + num + ")");
							}
							return 1;
						}
					}	
				} else {
					return -2;
				}
			}
		} catch(SQLException sqle) {
			sqle.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqle) {
				System.out.println("sqle: " + sqle.getMessage());
			}
		}
		return -3;
	}
}
<|editable_region_end|>
```