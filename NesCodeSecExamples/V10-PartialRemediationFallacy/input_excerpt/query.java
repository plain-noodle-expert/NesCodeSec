```<|start_of_file|>
<|editable_region_start|>
import java.sql.*;
import java.io.*;
import java.util.Scanner;

class query
{
  public static void main (String args [])
       throws SQLException
  {
    int a;
    int n;
    float b;
    Scanner reader = new Scanner(System.in);  // Reading from System.in

    String connstr = "jdbc:sqlite:states.db";
    Connection conn = DriverManager.getConnection (connstr);
	Statement stmt = conn.createStatement ();
	ResultSet rs;
  
	String state;
	
    while(true) { 
        n = reader.nextInt(); // Scans the next token of the input as an int.
 
        String q;
		if(n == 0) {
			System.out.println("System exit");
			System.exit(0);
		} else if (n == 1) {
			System.out.println("States Queries");
                        Boolean flag = true;
                        while(flag) {
                                n = reader.nextInt();

                                switch(n) {
						case 0:
								flag = false;
								break;
						case 1:
								q = "select state from states;";
								rs = stmt.executeQuery (q);
						System.out.println ("The 50 states are: ");
								while(rs.next()){
										System.out.println (rs.getString (1));                                                }
                        break;
                        case 2:
                                        System.out.println("Enter a zip code to see all other zip codes from the same city.");
                                                String zip;
						zip = reader.next();
                                                q = "select a.city, a.stcode, a.zip from uszips a, (select * from uszips z where z.zip = ? ) b where a.stcode = b.stcode and a.city=b.city;";
                                                //execute query with PreparedStatement
                                                PreparedStatement pstmt = conn.prepareStatement(q);
                                                pstmt.setString(1, zip);
                                                rs = pstmt.executeQuery();

					}
                                                
                        break;
                        case 3:
                                        System.out.println("Enter the beginning of the city names you would like to see.");
                                                String citystart;
                                                citystart = reader.nextLine();
                                                citystart = reader.nextLine();

                                                q = "select distinct city, stcode from uszips where city like '"+citystart+"%'";
                                                rs = stmt.executeQuery (q);
                        System.out.printf("These are the cities that begin with "+citystart+":\n");
                                                while(rs.next()){
                            System.out.printf(rs.getString (1)+", "+rs.getString (2)+"\n");
                        }
						break;
						default:
							System.out.println("Please enter a valid number");
							break;
						}

                        }
		} else if (n == 2) {
			System.out.println("ND Roster Queries");
			Boolean flag = true;
			while(flag) {
	
				n = reader.nextInt();
				
				switch(n) {
					case 0: 
						flag = false;
						break;
					case 1:  
        				System.out.println("Enter a state code to see which players are from that state");	
						state = reader.next();
						q = "select jno, last, first, city, stcode from roster where stcode = '"+state+"';";
					
						//execute query
						rs = stmt.executeQuery (q);
						if( !rs.next() ){ 
        					System.out.println ("There are no players from "+state+". ");
						} else {
        				System.out.println ("Players from "+state+": ");
						while(rs.next())
							System.out.println ("#"+rs.getString (1)+" "+rs.getString (3)+" "+rs.getString (2)+" is from "+rs.getString (4)+", "+rs.getString (5));
						}
                     	break;
            		case 2:  
        				System.out.println("Enter a state code to see how many players are from that state");	
						state = reader.next();
						q = "select count(*) from (select jno, last, first, city, stcode from roster where stcode = '"+state+"');";
					
						//execute query
						rs = stmt.executeQuery (q);
						if( !rs.next() ){ 
        					System.out.println ("There are no players from "+state+". ");
						} else {
        				System.out.println ("There are "+rs.getString(1)+" players from "+state);
						}
                     	break;
            		case 3:  
						q = "select r.region, players  from regions r, (select s.regcode as region, count(*) as players from streg s join roster x on s.stcode = x.stcode group by s.regcode order by 2 desc, 1) s where r.regcode=s.region order by 2 desc, 1";
						rs = stmt.executeQuery (q);
                        System.out.printf("%-15s %s\n", "Region", "Number of players");
						while(rs.next()){
                            System.out.printf("%-15s %2d\n", rs.getString(1), rs.getInt(2));
                        }
                     	break;
            		case 4:  
						q = "select position, avg(case when year = 'FR' then a.w end) as 'FR', avg(case when year = 'SO' then a.w end) as 'SO', avg(case when year = 'JR' then a.w end) as 'JR', avg(case when year = 'SR' then a.w end) as 'SR', avg(case when year = 'GS' then a.w end) as 'GS' from (select position, year, avg(weight) as w from roster group by position, year order by 2 desc, 1 ) a group by position order by 1;";
						//execute query
						rs = stmt.executeQuery (q);
						System.out.printf("%-20s %7s %7s %7s %7s %7s\n", "Position", "FR", "SO", "JR", "SR", "GS");
						while(rs.next()){
							System.out.printf("%-20s %7.1f %7.1f %7.1f %7.1f %7.1f\n", rs.getString(1), rs.getFloat(2), rs.getFloat(3), rs.getFloat(4), rs.getFloat(5), rs.getFloat(6));
						}
                     	break;
					case 5:
						q = "select region, avg(height_ft*12 + height_in) as avgheight, avg(weight) as avgweight from roster r, streg s, regions reg where r.stcode = s.stcode and reg.regcode = s.regcode group by s.regcode order by 2 desc, 3 desc, 1;";
						//execute query
						rs = stmt.executeQuery (q);
						System.out.printf("%-20s %12s %12s\n", "Region", "Avg Height", "Avg Weight");
						while(rs.next()){
							int feet = rs.getInt(2)/12;
							float in = rs.getFloat(2) - feet*12;
							System.out.printf("%-20s %6d'%4.1f\" %12.1f\n", rs.getString(1), feet, in, rs.getFloat(3));
						}
						break;
					case 6:
        				System.out.println("Enter a state code");	
						state = reader.next();
						q = "select case when p.team = 'O' then 'Offense' when p.team = 'D' then 'Defense' when p.team = 'S' then 'Special Teams' end as team, count(*) from roster r, positions p where r.position = p.position and r.stcode='"+state+"' group by team;";
						
						rs = stmt.executeQuery (q);
                        System.out.printf("%-15s %s\n", "Team", "Players from "+state);
						while(rs.next()){
                            System.out.printf("%-15s %-2d\n", rs.getString(1), rs.getInt(2));
                        }
						break;
					case 7:
						q = "select position, avg(weight) as avgWeight from roster group by position order by 2 desc, 1;";
						//execute query
						rs = stmt.executeQuery (q);
						System.out.printf("%-15s %12s\n", "Position", "Avg Weight");
						while(rs.next()){
							System.out.printf("%-15s %12.1f\n", rs.getString(1), rs.getFloat(2));
						}
						break;
					case 8:
        				System.out.println("Enter a position (ex: Running Back)");	
						state = reader.nextLine();
						state = reader.nextLine();
						q = "select jno, last, first from roster where position = '"+state+"';"<|user_cursor_is_here|>
						//execute query securely
						rs = stmt.executeQuery (q);
						if( !rs.next() ){ 
        					System.out.println ("There are no players that play "+state+". ");
						} else {
							System.out.println ("Players who play "+state+": ");
							while(rs.next())
								System.out.println ("#"+rs.getString (1)+" "+rs.getString (3)+" "+rs.getString (2));
						}
						break;
					default:
						System.out.println("Please enter a valid number");
						break;
				}

			}
		}
    }
  }
}
<|editable_region_end|>
```