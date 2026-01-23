```<|start_of_file|>
<|editable_region_start|>
//Back.java;
import java.awt.*;
import java.awt.event.*;
import java.io.Reader;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.text.*;
import javax.swing.JOptionPane;

public class Back extends Frame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7670129939284773294L;
	Label bookidlb = new Label("图书编号"), readeridlb = new Label("读者编号");
	TextField bookidtxt = new TextField(), readeridtxt = new TextField();
	Button querybtn = new Button("查询"), borrowbtn = new Button("还书"),
			closebtn = new Button("清除");
	String SepLine = "--------------------------------------------------";
	String[] sep = { "图书信息", "读者信息", "借阅信息" };
	Label[] seplabel = new Label[3];
	String[] optionname = { "书名：", "作者：", "出版社：", "出版时间：", "定价：", "存量：", "姓名：","类型：", "可借数：", "可借天数：", "借阅日期：","阅读天数：","还书日期" };
	Label[] alloption = new Label[13];
	Label[] showoption = new Label[13];

	public Back() {
		setTitle("图书归还");
		setLayout(null);
		setSize(500, 470);
		setResizable(false);
		this.setForeground(Color.BLACK);
		bookidlb.setBounds(50, 50, 50, 20);
		bookidtxt.setBounds(110, 50, 100, 20);
		readeridlb.setBounds(220, 50, 50, 20);
		readeridtxt.setBounds(280, 50, 100, 20);
		querybtn.setBounds(400, 50, 50, 20);
		add(bookidlb);
		add(bookidtxt);
		add(readeridlb);
		add(readeridtxt);
		add(querybtn);
		int lx = 50, ly = 90, i = 0, k = 0;
		for (int j = 0; j < alloption.length; j++) {
			if (lx > 300) {
				lx = 50;
				ly = ly + 30;
			}
			if (ly == 90 || ly == 210 || ly == 300) {
				System.out.println(i);// /太奇怪了
				seplabel[i] = new Label(SepLine + sep[i] + SepLine);
				seplabel[i].setBounds(20, ly, 440, 20);
				add(seplabel[i]);
				j--;
				k++;
				if (k <= 1) {
					i = 0;
				}
				if (k == 2 || k == 3) {
					i = 1;
				}
				if (k == 4) {
					i = 2;
				}
			} else {
				alloption[j] = new Label(optionname[j]);
				showoption[j] = new Label("");
				alloption[j].setBounds(lx, ly, 70, 20);
				showoption[j].setBounds(lx + 70, ly, 150, 20);
				add(alloption[j]);
				add(showoption[j]);
			}
			lx = lx + 250;
		}
		borrowbtn.setBounds(110, 400, 50, 20);
		closebtn.setBounds(310, 400, 50, 20);
		add(borrowbtn);
		add(closebtn);
		borrowbtn.setEnabled(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				//System.exit(0);
			}
		});
		querybtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				queryActionPerformed(e);
			}
		});

		borrowbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				borrowActionPerformed(e);
			}
		});
		closebtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setInitialize();	
			}
		});

		setLocationRelativeTo(null);
		setVisible(true);
	}
// TODO: fixed all sql injection issues
	public void borrowActionPerformed(ActionEvent e) {
		String bookid = bookidtxt.getText(), readerid = readeridtxt.getText();
		if (!bookid.equals("") && !readerid.equals("")) {
			Date currentdate = new Date();
			String borrowbackdate = showoption[12].getText();
			String sql = "UPDATE borrow " +
						"SET back_date = ?, if_back = ? " +
						"WHERE book_id = ? AND reader_id = ? AND if_back = ?";
			String sql1 = "UPDATE book " +
							"SET stock = ? " +
							"WHERE id = ?";
			Connection conn = DbOp.getConnection();
			PreparedStatement psBorrow = conn.prepareStatement(sql);<|user_cursor_is_here|>
			psBorrow.setDate(1, borrowbackdate);
			psBorrow.setString(2, "是");
			psBorrow.setString(3, bookid);
			psBorrow.setString(4, readerid);
			psBorrow.setString(5, "否");
         	PreparedStatement psBook = conn.prepareStatement(sql1);
			psBook.setInt(1, Integer.parseInt(showoption[5].getText()) + 1);
			psBook.setString(2, bookid);
			int success = psBorrow.executeUpdate();
			if (success == 1) {
				psBook.executeUpdate();
				JOptionPane.showMessageDialog(null, "还书成功");

			} else {
				JOptionPane.showMessageDialog(null, "还书数据登记失败！");
			}
			setInitialize();
		}
	}

	public Date getBorrowDate(String bookid, String readerid) {// 获取读者借书日期
		String sql, mydate = "";
		Date readerdate = new Date();
		sql = "select * from borrow where book_id='" + bookid
				+ "' and reader_id='" + readerid + "' and if_back='否'";
		ResultSet rs = DbOp.executeQuery(sql);
		try {
			if (rs.next()) {
				mydate = rs.getString("borrow_date");
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			readerdate = sdf.parse(mydate);
			DbOp.close();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "借书日期查询失败");
		} catch (ParseException e2) {
			JOptionPane.showMessageDialog(null, "借书日期异常");
		}
		return readerdate;
	}
}
<|editable_region_end|>
```
