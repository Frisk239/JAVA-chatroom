import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInit {
    public static void main(String[] args) {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/?useUnicode=true&characterEncoding=utf8&useSSL=false";
        String sqluser = "root";
        String sqlpassword = "123456";

        try {
            // 1. 测试数据库连接
            System.out.println("正在测试MySQL连接...");
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, sqluser, sqlpassword);
            if (!conn.isClosed()) {
                System.out.println("✅ MySQL连接成功!");
            }

            // 2. 创建chatroom数据库
            Statement stmt = conn.createStatement();
            System.out.println("正在创建chatroom数据库...");
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS chatroom DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("✅ chatroom数据库创建成功!");

            conn.close();

            // 3. 连接到chatroom数据库并运行初始化脚本
            String chatroomUrl = "jdbc:mysql://localhost:3306/chatroom?useUnicode=true&characterEncoding=utf8&useSSL=false";
            Connection chatroomConn = DriverManager.getConnection(chatroomUrl, sqluser, sqlpassword);
            Statement chatroomStmt = chatroomConn.createStatement();

            System.out.println("正在初始化数据库表结构...");

            // 创建用户表
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id VARCHAR(20) UNIQUE NOT NULL COMMENT '8位用户账号'," +
                "username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名'," +
                "password VARCHAR(255) NOT NULL COMMENT '密码（MD5加密）'," +
                "nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称'," +
                "avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL'," +
                "status TINYINT DEFAULT 1 COMMENT '状态：1=正常，2=禁用'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "last_login TIMESTAMP NULL COMMENT '最后登录时间'," +
                "PRIMARY KEY (user_id)," +
                "INDEX idx_username (username)," +
                "INDEX idx_status (status)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表'";

            chatroomStmt.executeUpdate(createUsersTable);
            System.out.println("✅ users表创建成功!");

            // 创建好友关系表
            String createFriendsTable = "CREATE TABLE IF NOT EXISTS friends (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id VARCHAR(20) NOT NULL COMMENT '用户ID'," +
                "friend_id VARCHAR(20) NOT NULL COMMENT '好友ID'," +
                "status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=待确认，1=已确认，2=已拒绝，3=已删除'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "UNIQUE KEY uk_user_friend (user_id, friend_id)," +
                "INDEX idx_user_id (user_id)," +
                "INDEX idx_friend_id (friend_id)," +
                "INDEX idx_status (status)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表'";

            chatroomStmt.executeUpdate(createFriendsTable);
            System.out.println("✅ friends表创建成功!");

            // 创建好友申请表
            String createFriendRequestsTable = "CREATE TABLE IF NOT EXISTS friend_requests (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "from_user_id VARCHAR(20) NOT NULL COMMENT '发起申请的用户ID'," +
                "to_user_id VARCHAR(20) NOT NULL COMMENT '接收申请的用户ID'," +
                "message VARCHAR(255) DEFAULT NULL COMMENT '申请留言'," +
                "status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=待处理，1=已同意，2=已拒绝'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间'," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '处理时间'," +
                "INDEX idx_from_user (from_user_id)," +
                "INDEX idx_to_user (to_user_id)," +
                "INDEX idx_status (status)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请表'";

            chatroomStmt.executeUpdate(createFriendRequestsTable);
            System.out.println("✅ friend_requests表创建成功!");

            chatroomStmt.close();
            chatroomConn.close();

            System.out.println("\n🎉 数据库初始化完成!");
            System.out.println("数据库连接信息:");
            System.out.println("  - 数据库: chatroom");
            System.out.println("  - 用户: root");
            System.out.println("  - 密码: 123456");
            System.out.println("  - 端口: 3306");

        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL驱动未找到: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ 数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}