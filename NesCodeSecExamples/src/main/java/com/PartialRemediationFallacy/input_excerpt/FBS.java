```<|start_of_file|>
<|editable_region_start|>
package models;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author MuhammadHarris
 */
public class FBS 
{
    
    public ArrayList<Flight> getAllFlights(Connection con, ArrayList<Customer> customers, ArrayList<Features> features)
    {
        ArrayList<Flight> flights = new ArrayList();        
        
        try
        {            
            Statement stmt = con.createStatement();
            String sql = "SELECT * FROM Flights";
            ResultSet rs = stmt.executeQuery(sql);

            
            while (rs.next())
            {
                boolean isChanged = rs.getBoolean("isChanged");

                String flightName = rs.getString("FlightName");

                int totalSeats = rs.getInt("TotalSeats");
                int currentSeats = rs.getInt("CurrentSeats");

                String departureCity = rs.getString("DepartureCity");
                String arrivalCity = rs.getString("ArrivalCity");

                Date departureDate = rs.getDate("DepartureDate");
                Date arrivalDate = rs.getDate("ArrivalDate");

                int economySeats = rs.getInt("EconomySeats");
                int businessSeats = rs.getInt("BusinessSeats");
                int firstSeats = rs.getInt("FirstSeats");

                int oldESeats = rs.getInt("OldESeats");
                int oldBSeats = rs.getInt("OldBSeats");
                int oldFSeats = rs.getInt("OldFSeats");
                int oldTSeats = rs.getInt("OldTSeats");

                Flight f = new Flight(isChanged, oldESeats, oldBSeats, oldFSeats, oldTSeats, flightName,null, totalSeats, currentSeats, departureCity, arrivalCity, departureDate, arrivalDate, economySeats, businessSeats, firstSeats);
                flights.add(f);                                                
              
                
                ArrayList<Seat> seats = new ArrayList();
                
                PreparedStatement stmt1 = con.prepareStatement("Select * from Seats where flightName = ?");
                stmt1.setString(1, flightName);
                ResultSet r = stmt1.executeQuery();<|user_cursor_is_here|>
                
                while(r.next())
                {
                                        
                    int seatnumber = r.getInt("SeatNumber");
                    String cEmail = r.getString("CustomerEmail");
                    String ftype = r.getString("FeatureType");
                    
                    Customer c = null;
                    for(int j = 0; j < customers.size(); j++)
                    {
                        if (customers.get(j).getEmail().equals(cEmail))
                        {    c = customers.get(j);
                             break;
                        }
                    }
                    
                    Features fet = null;
                    
                    if(ftype.equals("Economy"))
                        fet = features.get(0);
                    if(ftype.equals("Business"))
                        fet = features.get(1);
                    if(ftype.equals("First Class"))
                        fet = features.get(2);
                                        
                    Seat s = new Seat(seatnumber, f,fet,c);                    
                    seats.add(s);
                    
                    if (c!=null)
                        c.setSeat(s);
                }
                                
                f.setSeats(seats);                                                
            }

        }
        catch (SQLException ex) {
            Logger.getLogger(FBS.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }

        return flights;
    }
    
    
    //--------------Methods Exposed to Web Services---------------------//
    public static int getPrice(String origin, String destination)
    {
        int price = 0;
        //get db path
        String p = "";

        try 
        {
            p = new File(FBS.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath() + "\\airlines.accdb";
        }
        catch (URISyntaxException ex) 
        {
            System.out.println("Error loading database file.");
        }

        String host =   "jdbc:ucanaccess://" + p;

        // Step 1: Loading or registering Oracle JDBC driver class
        try {

            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        }
        catch(ClassNotFoundException cnfex) {

            System.out.println("Problem in loading or "
                    + "registering MS Access JDBC driver");
            cnfex.printStackTrace();
        }
        
        try 
        {
            Connection con = DriverManager.getConnection(host);            
            
            Statement statement = con.createStatement();
            
            String q = "SELECT * FROM FLIGHTS WHERE DEPARTURECITY = '" + origin + "' AND ARRIVALCITY = '" + destination + "' AND ECONOMYSEATS > 0";
            
            ResultSet r = statement.executeQuery(q);

            boolean isFound = false;
            
            while(r.next() && !isFound)
            {
                isFound = true;                                
            }
            
            if (isFound)
            {
                q = "SELECT PRICE FROM FEATURES WHERE TYPE = 0";
                r = statement.executeQuery(q);

                while (r.next())
                {
                   price = r.getInt(1);                    
                }
            }
            
            statement.close();
            r.close();                
            con.close();            
        }
        catch (SQLException ex) 
        {
            
        }
        
        return price;
    }        
    
    
    public static int getSeats(String flightName, String date)
    {
        int seats = 0;
        
        //get db path
        String p = "";

        try 
        {
            p = new File(FBS.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath() + "\\airlines.accdb";
        }
        catch (URISyntaxException ex) 
        {
            System.out.println("Error loading database file.");
        }

        String host =   "jdbc:ucanaccess://" + p;

        // Step 1: Loading or registering Oracle JDBC driver class
        try {

            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        }
        catch(ClassNotFoundException cnfex) {

            System.out.println("Problem in loading or "
                    + "registering MS Access JDBC driver");
            cnfex.printStackTrace();
        }
        
        try 
        {
            Connection con = DriverManager.getConnection(host);            
            
            Statement statement = con.createStatement();
            
            String q = "SELECT CURRENTSEATS FROM FLIGHTS WHERE FLIGHTNAME = '" + flightName + "' AND DEPARTUREDATE = ";
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            java.util.Date d = format.parse(date);            
            
            q += "TIMESTAMP '" + new Timestamp(d.getTime()).toString() + "'";
            
            ResultSet r = statement.executeQuery(q);

            boolean isFound = false;
            
            while(r.next())
            {
                seats = r.getInt(1);
            }
                        
            statement.close();
            r.close();                
            con.close();            
        }
        catch (SQLException | ParseException ex) 
        {
            
        }
        
        return seats;
    }        
    
    //-------------------------------FOR AUTOCOMPLETION THROUGH AJAX----------------------------//
    public ArrayList<String> getCities(Connection con, String city_name) 
    {
        ArrayList<String> list = new ArrayList();
        Statement ps = null;
        String data;
        try 
        {
            ps = con.createStatement();
            String query = "SELECT * FROM CITIES  WHERE CITYNAME  LIKE '" + city_name + "%'";
            ResultSet rs = ps.executeQuery(query);
            
            while (rs.next()) 
            {
                data = rs.getString("CITYNAME");
                list.add(data);
            }
        } 
        catch (Exception e) 
        {
            System.out.println(e.getMessage());
        }
        return list;
    }
}
<|editable_region_end|>
```