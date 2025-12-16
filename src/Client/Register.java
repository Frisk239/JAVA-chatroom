package Client;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import DB.UserDB;

public class Register extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextField userName; // 用户名
	private JPasswordField password; // 密码
	private JPasswordField password2; // 密码
	private JLabel lableUser;
	private JLabel lablePwd;
	private JLabel lablePwd2;
	private JButton btnRegister;

	public Register() {
		// 创建一个容器
		Container con = this.getContentPane();
		con.setBackground(new Color(240, 240, 240)); // 设置浅灰色背景
		// 用户名输入框
		userName = new JTextField();
		userName.setBounds(200, 50, 250, 35);
		userName.setFont(new Font("宋体", Font.PLAIN, 16));
		// 用户名标签
		lableUser = new JLabel("填写用户名");
		lableUser.setBounds(80, 50, 110, 35);
		lableUser.setFont(new Font("宋体", Font.PLAIN, 18));
		// 密码输入框
		password = new JPasswordField();
		password.setBounds(200, 100, 250, 35);
		password.setFont(new Font("宋体", Font.PLAIN, 16));
		password2 = new JPasswordField();
		password2.setBounds(200, 150, 250, 35);
		password2.setFont(new Font("宋体", Font.PLAIN, 16));
		// 密码标签
		lablePwd = new JLabel("填写密码");
		lablePwd.setBounds(80, 100, 110, 35);
		lablePwd.setFont(new Font("宋体", Font.PLAIN, 18));
		lablePwd2 = new JLabel("重填密码");
		lablePwd2.setBounds(80, 150, 110, 35);
		lablePwd2.setFont(new Font("宋体", Font.PLAIN, 18));
		// 添加提示标签
		JLabel hintLabel = new JLabel("系统将自动为您分配8位数字账号");
		hintLabel.setBounds(180, 200, 300, 30);
		hintLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		hintLabel.setForeground(Color.GRAY);

		// 注册按钮
		btnRegister = new JButton("注册");
		btnRegister.setBounds(250, 240, 150, 40);
		btnRegister.setFont(new Font("宋体", Font.PLAIN, 18));
		btnRegister.setBackground(new Color(255, 182, 193)); // 使用粉色主题
		btnRegister.setForeground(Color.WHITE);
		btnRegister.setFocusPainted(false);
		btnRegister.setBorderPainted(false);
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = userName.getText();
				String pwd1 = String.valueOf(password.getPassword());
				String pwd2 = String.valueOf(password2.getPassword());
				registerUser(name, pwd1, pwd2);
			}
		});
		// 将组件添加到容器
		con.add(lableUser);
		con.add(lablePwd);
		con.add(lablePwd2);
		con.add(hintLabel);
		con.add(userName);
		con.add(password);
		con.add(password2);
		con.add(btnRegister);
		this.setTitle("注册窗口");// 设置窗口标题
		this.setLayout(null);// 设置布局方式为自定义位置
		this.setBounds(0, 0, 550, 320);
		this.setResizable(false);// 窗口大小不可改变
		this.setLocationRelativeTo(null);// 居中显示
		this.setVisible(true);// 窗口可见
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	}

	// 注册方法
	private void registerUser(String name, String pwd1, String pwd2) {
		if (pwd1.equals(pwd2)) {
			// 验证用户名不为空
			if (name == null || name.trim().isEmpty()) {
				JOptionPane.showMessageDialog(new JFrame(), "用户名不能为空！", "错误",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// 验证密码长度
			if (pwd1.length() < 6) {
				JOptionPane.showMessageDialog(new JFrame(), "密码长度至少6位！", "错误",
						JOptionPane.ERROR_MESSAGE);
				password.setText("");
				password2.setText("");
				return;
			}

			UserDB c = new UserDB(name, pwd2);
			if (c.addsql() == true) {
				// 注册成功，获取用户信息
				String[] userInfo = c.getUserInfo(name);
				String assignedUserId = userInfo != null ? userInfo[0] : "未知";

				JOptionPane.showMessageDialog(new JFrame(),
						"注册成功！\n您的用户名：" + name + "\n您的账号：" + assignedUserId + "\n请妥善保管账号和密码",
						"注册成功", JOptionPane.INFORMATION_MESSAGE);
				this.setVisible(false); // 关闭注册窗口
				new Client(name); // 打开聊天界面
			} else {
				JOptionPane.showMessageDialog(new JFrame(), "注册失败，该用户名已存在", "错误",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(new JFrame(), "两次输入的密码不一致！", "错误",
					JOptionPane.ERROR_MESSAGE);
			password.setText("");
			password2.setText("");
		}
	}
}
