import DB.UserDB;
import java.util.Scanner;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        System.out.println("测试数据库连接和好友管理功能...");

        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入MySQL密码（默认123456，直接回车）：");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) {
            password = "123456";
        }

        // 测试数据库连接
        // 由于sqlpassword是私有字段，我们直接使用默认的123456
        UserDB userDB = new UserDB("", password);

        try {
            // 测试获取用户信息
            System.out.println("测试数据库连接...");
            String[] userInfo = userDB.getUserInfo("10000");
            if (userInfo != null) {
                System.out.println("数据库连接成功！");
                System.out.println("用户ID: " + userInfo[0]);
                System.out.println("用户名: " + userInfo[1]);
                System.out.println("昵称: " + userInfo[2]);
            } else {
                System.out.println("数据库连接成功，但没有找到用户10000");
            }

            // 测试好友列表
            System.out.println("测试好友列表功能...");
            try {
                var friends = userDB.getFriendList("10000");
                System.out.println("好友列表查询成功，共 " + friends.size() + " 个好友");
            } catch (Exception e) {
                System.out.println("好友列表查询失败: " + e.getMessage());
            }

            // 测试用户搜索
            System.out.println("测试用户搜索功能...");
            try {
                var searchResults = userDB.searchUsers("test", "10000");
                System.out.println("用户搜索成功，找到 " + searchResults.size() + " 个用户");
            } catch (Exception e) {
                System.out.println("用户搜索失败: " + e.getMessage());
            }

            System.out.println("数据库功能测试完成！");

        } catch (Exception e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            e.printStackTrace();
        }

        scanner.close();
    }
}