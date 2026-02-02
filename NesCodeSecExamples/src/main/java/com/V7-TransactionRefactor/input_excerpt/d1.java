```<|start_of_file|>
<|editable_region_start|>
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.*;
import javax.accessibility.*;
class demo extends JFrame implements ActionListener
{
      private static final Logger logger = LoggerFactory.getLogger(demo.class);
      Font f=new Font("sans-serif",Font.BOLD,20);
      JPanel p=new JPanel();
      JPanel p1=new JPanel();
      ImageIcon login=new ImageIcon("C:\\Users\\hem viraj naik\\Desktop\\login\\login.jpg");
      ImageIcon username=new ImageIcon("C:\\Users\\hem viraj naik\\Desktop\\username.jpg");
      ImageIcon password=new ImageIcon("C:\\Users\\hem viraj naik\\Desktop\\password.jpg");
      ImageIcon admin=new ImageIcon("C:\\Users\\hem viraj naik\\Desktop\\admin.jpg");
      ImageIcon submit=new ImageIcon("C:\\Users\\hem viraj naik\\Desktop\\submit.jpg");
      ImageIcon refresh=new ImageIcon("C:\\Users\\hem viraj naik\\Desktop\\refresh.jpg");
      JLabel l=new JLabel(login);
      JLabel l2=new JLabel(password);
      JLabel l1=new JLabel(username);
      JLabel l3=new JLabel(admin);


      JTextField t1=new JTextField();
      JPasswordField t2=new JPasswordField();
      JComboBox c=new JComboBox();
      JButton b1=new JButton("Login",submit);
      JButton b2=new JButton("Refresh",refresh);
      public void actionPerformed(ActionEvent k)
      {
            if(k.getSource()==b1) {

                  String username=t1.getText();
                  String password=t2.getText();
                  String user_type=c.getSelectedItem().toString();
                  logger.info("Attempting login for user: " + username + " with user type: " + user_type);
                  try {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/loginsystem", "root", "password");
                        JOptionPane.showMessageDialog(null,"Connectionn Established!!!","Connection Status",JOptionPane.INFORMATION_MESSAGE);
                       
                        String sqlquery="SELECT * FROM user_detail WHERE password LIKE"+'"'+password+'"';
                        Statement s=con.createStatement();
                        logger.info("Executing SQL query"<|user_cursor_is_here|>);
                        s.execute(sqlquery);
                        ResultSet rs=s.executeQuery(sqlquery);
                        while(rs.next())
                        {
                              if(username.equals(rs.getString(1))&&password.equals(rs.getString(2))&&user_type.equals(rs.getString(3)))
                              {
                                    logger.info("Login successful");
                                     JOptionPane.showMessageDialog(null,"Login Successful!!!","Login Status",JOptionPane.INFORMATION_MESSAGE);
                              }
                              else
                              {
                                    logger.warn("Login failed");
                                    JOptionPane.showMessageDialog(null,"Login Failed!!!","Login Status",JOptionPane.INFORMATION_MESSAGE);
                              }
                        }
                      }
                  
               
                  catch(Exception k1)
                  {
                        //JOptionPane.showMessageDialog(null,"Connection failed","Connection Status",JOptionPane.ERROR_MESSAGE);
						System.out.println(k1);
                  }
            }
            if(k.getActionCommand()=="Refresh")
            {
                  int x=JOptionPane.showConfirmDialog(null,"Do you want to Refresh?","Refresh",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
                  if(x==0){
                        t1.setText(null);
                        t2.setText(null);
                        c.setSelectedIndex(0);
                        JOptionPane.showMessageDialog(null,"Fields refreshed!!!","Refresh Message",JOPtionPane.INFORMATION_MESSAGE);
                  }
                  else{
                        repaint();
                  }

            }
      }
}
public class d1
{
      public static void main(String arguments[])
      {
            demo d=new demo();
      }
}
<|editable_region_end|>
```