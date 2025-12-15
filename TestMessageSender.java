import java.io.*;
import java.net.*;

public class TestMessageSender {
    public static void main(String[] args) {
        try {
            // 连接到服务器
            Socket socket = new Socket("localhost", 8888);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            
            // 发送测试消息
            System.out.println("发送测试消息...");
            writer.println("test@ALL@这是一条测试消息，用于验证乱码修复和多用户聊天功能！");
            
            // 等待响应（可选）
            Thread.sleep(1000);
            
            // 关闭连接
            writer.close();
            reader.close();
            socket.close();
            
            System.out.println("测试消息发送完成！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}