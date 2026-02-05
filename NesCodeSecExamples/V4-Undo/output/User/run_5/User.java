<|editable_region_start|>
package Final;

import java.sql.*;

import javafx.beans.property.SimpleStringProperty;

/*
 * This class is a data model that works mostly with the User table
 * 
 * it implements a search method as required by the TableInterface which it's abstract super 
 * class TableModel implements
 */
public class User extends TableModel {
	
	
	public User() {
		//reference superclass TableModel for documentation on these instance vars
		this.properties.put("UserID", new SimpleStringProperty(""));
		this.properties.put("Name", new SimpleStringProperty(""));
		this.properties.put("Address", new SimpleStringProperty(""));
		this.searchKey = "UserID";
		this.primaryKey = "uid";
		this.modelName = "User";

	}
	
	//implementation required by interface, search by user id 
	public String search() throws SQLException{
		String resultText = "";
		String id = getId();
		
		//validate user id field before attempting query
		if(searchValid()){
			
			//check that user of this id actually exists
			if(isValidId(id)){
				
				//create statement using global connection object and execute query selecting user information from user table
				Statement search = Final.connection.createStatement();
				ResultSet rs = search.executeQuery("select uid, name, address from User where uid='"+id+"'");

				//only one record expected to be returned, so move cursor to first position
				rs.first();
				
				//parse results to property map values, automatically updating UI due to binding
				this.properties.get("UserID").set(rs.getString("uid"));
				this.properties.get("Name").set(rs.getString("name"));
				this.properties.get("Address").set(rs.getString("address"));
				
				resultText = "Searched for User";
				
			}else{
				//not a valid user id
				resultText = "There is no user with that ID";
			}

		}else{
			//bad user id search string
			resultText = "Invalid Or Blank UserID";
		}
		return resultText;

	}
	
	

}
<|editable_region_end|>
```
