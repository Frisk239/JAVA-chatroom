package DB;

import java.sql.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class UserDB {
	String driver = "com.mysql.jdbc.Driver";
	String url = "jdbc:mysql://localhost:3306/user?useUnicode=true&characterEncoding=utf8&useSSL=false";
	String sqluser = "root";
	String sqlpassword = "123456";

	String userpwd_;
	String username_;
	boolean n = false;

	public UserDB(String name, String pwd) {
		username_ = name;
		userpwd_ = pwd;
	}

	public Boolean selectsql() {
		n = false;
		System.out.println("开始登录验证，用户：" + username_ + "，密码：" + userpwd_);
		try {
			Class.forName(driver);
			System.out.println("驱动加载成功");
			Connection conn = DriverManager.getConnection(url, sqluser,
				sqlpassword);
			if (!conn.isClosed())
				System.out.println("连接数据库成功!");
			Statement statement = conn.createStatement();
			String sql = "select userpwd from info where username=" + "'" + username_ + "';";
			System.out.println("执行SQL查询：" + sql);
			ResultSet rs = statement.executeQuery(sql);
			String readpwd = null;
			if (rs.next()) {
				readpwd = rs.getString("userpwd");
				System.out.println("数据库中查询到的密码：" + readpwd);
				if (readpwd.equals(userpwd_)) {
					System.out.println("密码匹配，登录成功");
					n = true;
				} else {
					System.out.println("密码不匹配，登录失败");
				}
			} else {
				System.out.println("未找到该用户：" + username_);
			}
			rs.close();
			conn.close();
		} catch (ClassNotFoundException e) {
			System.out.println("加载MySQL驱动失败!" + e.getMessage());
		} catch (SQLException e1) {
			System.out.println("SQL异常:" + e1.getMessage());
		} catch (Exception e2) {
			System.out.println("其他异常:" + e2.getMessage());
		}
		System.out.println("登录验证结果：" + n);
		return n;
	}

	public boolean addsql() {
		int count = 0;
		n = false;
		try {
			// 加载驱动
			Class.forName(driver);
			// 连接数据库
			Connection conn = DriverManager.getConnection(url, sqluser,
				sqlpassword);
			if (!conn.isClosed())
				System.out.println("连接数据库成功!");

			// 先检查用户名是否已存在
			Statement checkStmt = conn.createStatement();
			String checkSql = "select username from info where username='" + username_ + "';";
			ResultSet rs = checkStmt.executeQuery(checkSql);
			if (rs.next()) {
				// 用户名已存在
				System.out.println("用户名已存在: " + username_);
				JOptionPane.showMessageDialog(new JFrame(), "该用户名已存在", "提示",
					JOptionPane.ERROR_MESSAGE);
				rs.close();
				checkStmt.close();
				conn.close();
				return false;
			}
			rs.close();
			checkStmt.close();

			// 用户名不存在，执行插入操作
			String sql = "insert into info (username, userpwd) values (?,?);";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, username_);
			ps.setString(2, userpwd_);
			count = ps.executeUpdate();
			// 检查insert语句是否执行成功（count > 0表示插入成功）
			if (count > 0)
				{ 
					n = true;
					System.out.println("注册成功***");
				}
			else {
					JOptionPane.showMessageDialog(new JFrame(), "注册失败", "提示",
						JOptionPane.ERROR_MESSAGE);
				}
			ps.close();
			conn.close();
		} catch (ClassNotFoundException e) {
			System.out.println("加载MySQL驱动失败!");
		} catch (SQLException e) {
			System.out.println("SQL异常: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("其他异常: " + e.getMessage());
			e.printStackTrace();
		}
		return n;
	}

}