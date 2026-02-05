import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class C1 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection=(Connection) DriverManager.getConnection("jdbc:mysql://localhost/test", "root", "root");
			Scanner scanner=new Scanner(System.in);
			System.out.println("Enter URL: ");
			String longUrl=scanner.nextLine();
			String rand=String.valueOf(Math.random()*100000);
			StringTokenizer stringTokenizer=new StringTokenizer(rand, ".");
			rand=stringTokenizer.nextToken();
			String baseUrl="http://www.goo.gl.com/";
			Statement statement=(Statement)connection.createStatement();
			String shortenedUrl=baseUrl + rand;
			statement.execute("INSERT INTO url_shortner_table_1 VALUES ('" + longUrl + "', '" + shortenedUrl + "')");
			System.out.println(shortenedUrl);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
}