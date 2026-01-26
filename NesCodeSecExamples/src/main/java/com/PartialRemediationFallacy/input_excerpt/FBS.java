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
            
            String q = "SELECT CURRENTSEATS FROM FLIGHTS WHERE FLIGHTNAME = '" <|user_cursor_is_here|>
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
        PreparedStatement ps = null;
        String data;
        try 
        {
            ps = con.prepareStatement("SELECT * FROM CITIES WHERE CITYNAME LIKE ?");
            ps.setString(1, city_name + "%");
            ResultSet rs = ps.executeQuery();
            
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