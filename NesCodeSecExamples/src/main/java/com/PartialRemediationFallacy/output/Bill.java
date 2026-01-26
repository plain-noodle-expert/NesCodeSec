<|editable_region_start|>
import java.io.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.border.LineBorder;

class Bill extends JFrame implements ActionListener
{
	private JPanel backInv;
	private JDialog invo;
	private Variables var;
	
	private Container cpane;
	private JLabel blno,dat,cname,phon,addr;
	private java.awt.List sno1,desc1,qnt1,rat1,disc1,netamt1;
	private JLabel blno1,cname1,phon1,addr1,head1,head2,tabs;
	private JLabel tot,pic,vat,disnt,disc,total,pacfor,totsal,lTotal;
	private JTextField disnt1,total1,pacfor1;
	private JButton print,save,home;
	private Font f1,f2;
	private JSeparator s1,s2,s3,s4;
	
	private int iCustID=0;
		
	private float fFinalCost=0,temp,temp1,temp2;
	
	
	public void setCustomer()
	{
		// CONNECTING TO DATABASE
			try
			{
				Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
				Connection con = DriverManager.getConnection("jdbc:odbc:Auto spare","","");
				Statement st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
				ResultSet rs = st.executeQuery("select * from Customer where Name='"+var.sCustomerName+"'");
				rs.first();
				
				iCustID = rs.getInt("ID");
				cname1.setText(cname1.getText()+" "+rs.getString("Name"));
				phon1.setText(phon1.getText()+" "+rs.getInt("Phone"));
				addr1.setText(addr1.getText()+" "+rs.getString("Address"));
			}
			catch(SQLException se)
			{
				System.out.print(se);
				JOptionPane.showMessageDialog(this, "Sorry, unable to connect to database !!");
				invo.dispose();
			}
	}
	
	public void saveRecord()
	{
		// SAVING THE RECORD TO DATABASE
		  try
		  {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			Connection con = DriverManager.getConnection("jdbc:odbc:Auto spare","","");
			Statement st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			ResultSet rs=st.executeQuery("select * from BillReport");
			rs.last();
			int iBillID = (1+rs.getInt("Bill ID"));
			iCustID = getID();
			int iResult = st.executeUpdate("insert into BillReport values("+iBillID+","+iCustID+","+fFinalCost+",'"+var.tDate.getText()+"')");
			if(iResult==1)
			{
				// If Query succesful
				System.out.print("pass");
			}
			else
			{
				//  Query failed
				System.out.print("fail");
			}
	   	  }
	      catch(Exception e)
	      {
	      	System.out.print(e);
	      	JOptionPane.showMessageDialog(this, "Sorry, unable to connect to database !!");
	      	invo.dispose();
	      }
	}
	
	public void updateDatabase(String sTempPart,int iTempQuantity)
	{
		try
		{
			Connection con = DriverManager.getConnection("jdbc:odbc:Auto spare");
			Statement st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery("select * from Spare where Name='"+sTempPart+"'");
			rs.first();
			int iQuantity = rs.getInt("Quantity");
			int temp = iQuantity-iTempQuantity;
			
			PreparedStatement upd = con.prepareStatement("update Spare set Quantity=? where Name=?");
			upd.setInt(1, temp);
			upd.setString(2, sTempPart);
			int iResult = upd.executeUpdate();
			if(iResult==1)
			{
				// If Query succesful
				//System.out.print(" : Pass : ");
			}
			else
			{
				//  Query failed
				//System.out.print(" : Fail : "+iResult);
			}
		}
		catch(SQLException e)
		{
			System.out.print(e);
			JOptionPane.showMessageDialog(this,"Unable to connect to database");
		}
	}

}
<|editable_region_end|>
```
