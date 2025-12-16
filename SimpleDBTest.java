import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SimpleDBTest {
    public static void main(String[] args) {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/chatroom?useUnicode=true&characterEncoding=utf8&useSSL=false";
        String sqluser = "root";
        String sqlpassword = "123456";

        try {
            System.out.println("正在加载数据库驱动...");
            Class.forName(driver);
            System.out.println("✅ 数据库驱动加载成功");

            System.out.println("正在连接数据库...");
            Connection conn = DriverManager.getConnection(url, sqluser, sqlpassword);
            if (!conn.isClosed()) {
                System.out.println("✅ 数据库连接成功!");
            }

            // 测试查询
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema = 'chatroom'");

            if (rs.next()) {
                int count = rs.getInt("table_count");
                System.out.println("✅ 查询成功! chatroom数据库中有 " + count + " 张表");
            }

            // 显示所有表
            rs = stmt.executeQuery("SHOW TABLES");
            System.out.println("数据库表列表:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString(1));
            }

            rs.close();
            stmt.close();
            conn.close();

            System.out.println("✅ 数据库测试完成!");

        } catch (ClassNotFoundException e) {
            System.out.println("❌ 数据库驱动未找到: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ 数据库操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}