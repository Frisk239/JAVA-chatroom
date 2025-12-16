package DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import Model.Friend;
import Model.FriendRequest;

public class UserDB {
	String driver = "com.mysql.cj.jdbc.Driver";
	String url = "jdbc:mysql://localhost:3306/chatroom?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
	String sqluser = "root";
	String sqlpassword = "123456";

	String userpwd_;
	String username_;
	boolean n = false;
	private static final Random random = new Random();

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
			Connection conn = DriverManager.getConnection(url, sqluser, sqlpassword);
			if (!conn.isClosed())
				System.out.println("连接数据库成功!");

			// 支持用户名或账号登录
			String sql = "SELECT password, username FROM users WHERE username = ? OR user_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, username_);
			ps.setString(2, username_);
			System.out.println("执行SQL查询：" + sql);

			ResultSet rs = ps.executeQuery();
			String readpwd = null;
			String dbUsername = null;
			if (rs.next()) {
				readpwd = rs.getString("password");
				dbUsername = rs.getString("username");
				System.out.println("数据库中查询到的密码：" + readpwd);
				// 将输入的密码进行MD5加密后比较
				String inputPasswordMD5 = MD5(userpwd_);
				System.out.println("输入密码的MD5值：" + inputPasswordMD5);
				if (readpwd != null && readpwd.equals(inputPasswordMD5)) {
					System.out.println("密码匹配，登录成功");
					n = true;
					// 更新最后登录时间
					updateLastLoginTime(username_);
				} else {
					System.out.println("密码不匹配，登录失败");
				}
			} else {
				System.out.println("未找到该用户：" + username_);
			}
			rs.close();
			ps.close();
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

	/**
	 * 更新用户最后登录时间
	 */
	private void updateLastLoginTime(String username) {
		try {
			Connection conn = DriverManager.getConnection(url, sqluser, sqlpassword);
			String sql = "UPDATE users SET last_login = NOW() WHERE username = ? OR user_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, username);
			ps.executeUpdate();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("更新登录时间失败:" + e.getMessage());
		}
	}

	public boolean addsql() {
		return addsql(username_, userpwd_);
	}

	/**
	 * 注册用户，自动分配8位账号
	 */
	public boolean addsql(String username, String password) {
		int count = 0;
		n = false;
		Connection conn = null;
		try {
			// 加载驱动
			Class.forName(driver);
			// 连接数据库
			conn = DriverManager.getConnection(url, sqluser, sqlpassword);
			if (!conn.isClosed())
				System.out.println("连接数据库成功!");

			// 检查用户名是否已存在
			String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
			PreparedStatement checkPs = conn.prepareStatement(checkSql);
			checkPs.setString(1, username);
			ResultSet rs = checkPs.executeQuery();

			if (rs.next() && rs.getInt(1) > 0) {
				// 用户名已存在
				System.out.println("用户名已存在: " + username);
				JOptionPane.showMessageDialog(new JFrame(), "该用户名已存在", "提示",
					JOptionPane.ERROR_MESSAGE);
				rs.close();
				checkPs.close();
				return false;
			}
			rs.close();
			checkPs.close();

			// 生成8位账号
			String userId = generateUserId();
			while (isUserIdExists(conn, userId)) {
				userId = generateUserId(); // 如果重复则重新生成
			}

			// 插入用户信息
			String sql = "INSERT INTO users (user_id, username, password, nickname, status) VALUES (?, ?, ?, ?, 1)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, username);
			ps.setString(3, password); // 实际应用中应该使用加密
			ps.setString(4, username); // 默认昵称与用户名相同

			count = ps.executeUpdate();

			if (count > 0) {
				n = true;
				System.out.println("注册成功，分配账号: " + userId);
				JOptionPane.showMessageDialog(new JFrame(), "注册成功！您的账号是：" + userId, "注册成功",
					JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(new JFrame(), "注册失败", "提示",
					JOptionPane.ERROR_MESSAGE);
			}
			ps.close();

		} catch (ClassNotFoundException e) {
			System.out.println("加载MySQL驱动失败!" + e.getMessage());
		} catch (SQLException e) {
			System.out.println("SQL异常: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("其他异常: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) conn.close();
			} catch (SQLException e) {
				System.out.println("关闭数据库连接失败:" + e.getMessage());
			}
		}
		return n;
	}

	/**
	 * 生成8位随机账号
	 */
	private String generateUserId() {
		return String.format("%08d", random.nextInt(90000000) + 10000000);
	}

	/**
	 * 检查账号是否已存在
	 */
	private boolean isUserIdExists(Connection conn, String userId) throws SQLException {
		String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, userId);
		ResultSet rs = ps.executeQuery();
		boolean exists = rs.next() && rs.getInt(1) > 0;
		rs.close();
		ps.close();
		return exists;
	}

	/**
	 * 获取用户信息
	 */
	public String[] getUserInfo(String usernameOrId) {
		String[] userInfo = null;
		try {
			Connection conn = DriverManager.getConnection(url, sqluser, sqlpassword);
			String sql = "SELECT user_id, username, nickname FROM users WHERE username = ? OR user_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, usernameOrId);
			ps.setString(2, usernameOrId);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				userInfo = new String[3];
				userInfo[0] = rs.getString("user_id");
				userInfo[1] = rs.getString("username");
				userInfo[2] = rs.getString("nickname");
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("获取用户信息失败:" + e.getMessage());
		}
		return userInfo;
	}

	// ========== 好友管理相关方法 ==========

	/**
	 * 发送好友申请
	 */
	public boolean sendFriendRequest(String fromUserId, String toUserId, String message) {
		try {
			Connection conn = DriverManager.getConnection(url, sqluser, sqlpassword);

			// 检查是否已经申请过或者已经是好友
			String checkSql = "SELECT COUNT(*) FROM friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
			PreparedStatement checkPs = conn.prepareStatement(checkSql);
			checkPs.setString(1, fromUserId);
			checkPs.setString(2, toUserId);
			checkPs.setString(3, toUserId);
			checkPs.setString(4, fromUserId);
			ResultSet rs = checkPs.executeQuery();

			if (rs.next() && rs.getInt(1) > 0) {
				rs.close();
				checkPs.close();
				conn.close();
				return false; // 已经存在好友关系
			}
			rs.close();
			checkPs.close();

			// 插入好友申请
			String sql = "INSERT INTO friend_requests (from_user_id, to_user_id, message, status) VALUES (?, ?, ?, 0)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, fromUserId);
			ps.setString(2, toUserId);
			ps.setString(3, message);

			int count = ps.executeUpdate();
			ps.close();
			conn.close();

			return count > 0;
		} catch (SQLException e) {
			System.out.println("发送好友申请失败:" + e.getMessage());
			return false;
		}
	}

	/**
	 * 处理好友申请
	 */
	public boolean processFriendRequest(int requestId, String fromUserId, String toUserId, int status) {
		try {
			Connection conn = DriverManager.getConnection(url, sqluser, sqlpassword);
			conn.setAutoCommit(false); // 开始事务

			try {
				// 更新申请状态
				String updateRequestSql = "UPDATE friend_requests SET status = ?, updated_at = NOW() WHERE id = ?";
				PreparedStatement updatePs = conn.prepareStatement(updateRequestSql);
				updatePs.setInt(1, status);
				updatePs.setInt(2, requestId);
				updatePs.executeUpdate();
				updatePs.close();

				// 如果同意申请，添加好友关系
				if (status == 1) {
					// 添加双向好友关系
					String addFriendSql = "INSERT INTO friends (user_id, friend_id, status, created_at) VALUES (?, ?, 1, NOW()), (?, ?, 1, NOW())";
					PreparedStatement addPs = conn.prepareStatement(addFriendSql);
					addPs.setString(1, fromUserId);
					addPs.setString(2, toUserId);
					addPs.setString(3, toUserId);
					addPs.setString(4, fromUserId);
					addPs.executeUpdate();
					addPs.close();
				}

				conn.commit(); // 提交事务
				return true;
			} catch (SQLException e) {
				conn.rollback(); // 回滚事务
				throw e;
			} finally {
				conn.setAutoCommit(true);
				conn.close();
			}
		} catch (SQLException e) {
			System.out.println("处理好友申请失败:" + e.getMessage());
			return false;
		}
	}

	/**
	 * 获取用户的好友列表
	 */
	public List<Friend> getFriendList(String userId) {
		List<Friend> friendList = new ArrayList<>();
		try {
			Connection conn = DriverManager.getConnection(url, sqluser, sqlpassword);
			String sql = "SELECT f.*, u.username, u.nickname, u.avatar FROM friends f " +
						"JOIN users u ON f.friend_id = u.user_id " +
						"WHERE f.user_id = ? AND f.status = 1";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Friend friend = new Friend();
				friend.setId(rs.getInt("id"));
				friend.setUserId(rs.getString("user_id"));
				friend.setFriendId(rs.getString("friend_id"));
				friend.setStatus(rs.getInt("status"));
				friend.setCreatedTime(rs.getTimestamp("created_at"));
				friend.setUpdatedTime(rs.getTimestamp("updated_at"));
				friend.setFriendNickname(rs.getString("nickname"));
				friend.setFriendAvatar(rs.getString("avatar"));
				friend.setFriendRemark(rs.getString("nickname")); // 暂时使用昵称作为备注
				friendList.add(friend);
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("获取好友列表失败:" + e.getMessage());
		}
		return friendList;
	}

	/**
	 * 获取待处理的好友申请
	 */
	public List<FriendRequest> getPendingFriendRequests(String userId) {
		List<FriendRequest> requestList = new ArrayList<>();
		try {
			Connection conn = DriverManager.getConnection(url, sqluser, sqlpassword);
			String sql = "SELECT fr.*, u1.nickname as from_nickname, u1.avatar as from_avatar, " +
						"u2.nickname as to_nickname FROM friend_requests fr " +
						"JOIN users u1 ON fr.from_user_id = u1.user_id " +
						"JOIN users u2 ON fr.to_user_id = u2.user_id " +
						"WHERE fr.to_user_id = ? AND fr.status = 0 ORDER BY fr.created_at DESC";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				FriendRequest request = new FriendRequest();
				request.setId(rs.getInt("id"));
				request.setFromUserId(rs.getString("from_user_id"));
				request.setToUserId(rs.getString("to_user_id"));
				request.setMessage(rs.getString("message"));
				request.setStatus(rs.getInt("status"));
				request.setCreatedTime(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : "");
				request.setProcessedTime(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toString() : "");
				request.setFromUserNickname(rs.getString("from_nickname"));
				request.setToUserNickname(rs.getString("to_nickname"));
				request.setFromUserAvatar(rs.getString("from_avatar"));
				requestList.add(request);
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("获取好友申请失败:" + e.getMessage());
		}
		return requestList;
	}

	/**
	 * 搜索用户
	 */
	public List<String[]> searchUsers(String keyword, String currentUserId) {
		List<String[]> userList = new ArrayList<>();
		try {
			Connection conn = DriverManager.getConnection(url, sqluser, sqlpassword);
			String sql = "SELECT user_id, username, nickname, avatar FROM users " +
						"WHERE (user_id = ? OR username LIKE ? OR nickname LIKE ?) AND user_id != ? " +
						"AND status = 1 LIMIT 20";
			PreparedStatement ps = conn.prepareStatement(sql);
			// 精确匹配user_id，模糊匹配username和nickname
			ps.setString(1, keyword);  // 精确匹配账号
			ps.setString(2, "%" + keyword + "%");  // 模糊匹配用户名
			ps.setString(3, "%" + keyword + "%");  // 模糊匹配昵称
			ps.setString(4, currentUserId);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String[] userInfo = new String[4];
				userInfo[0] = rs.getString("user_id");
				userInfo[1] = rs.getString("username");
				userInfo[2] = rs.getString("nickname");
				userInfo[3] = rs.getString("avatar");
				userList.add(userInfo);
			}
			rs.close();
			ps.close();
			conn.close();

			System.out.println("搜索关键词 '" + keyword + "' 找到 " + userList.size() + " 个用户");
		} catch (SQLException e) {
			System.out.println("搜索用户失败:" + e.getMessage());
		}
		return userList;
	}

	/**
	 * 删除好友
	 */
	public boolean deleteFriend(String userId, String friendId) {
		try {
			Connection conn = DriverManager.getConnection(url, sqluser, sqlpassword);
			String sql = "DELETE FROM friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, friendId);
			ps.setString(3, friendId);
			ps.setString(4, userId);

			int count = ps.executeUpdate();
			ps.close();
			conn.close();

			return count > 0;
		} catch (SQLException e) {
			System.out.println("删除好友失败:" + e.getMessage());
			return false;
		}
	}

	/**
	 * 检查是否为好友关系
	 */
	public boolean isFriend(String userId1, String userId2) {
		try {
			Connection conn = DriverManager.getConnection(url, sqluser, sqlpassword);
			String sql = "SELECT COUNT(*) FROM friends WHERE " +
						"((user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)) " +
						"AND status = 1";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId1);
			ps.setString(2, userId2);
			ps.setString(3, userId2);
			ps.setString(4, userId1);

			ResultSet rs = ps.executeQuery();
			boolean isFriend = rs.next() && rs.getInt(1) > 0;
			rs.close();
			ps.close();
			conn.close();

			return isFriend;
		} catch (SQLException e) {
			System.out.println("检查好友关系失败:" + e.getMessage());
			return false;
		}
	}

	/**
	 * MD5加密方法
	 */
	private String MD5(String input) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(input.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			System.out.println("MD5加密失败:" + e.getMessage());
			return null;
		}
	}

}