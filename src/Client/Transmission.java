package Client;

import java.util.UUID;

public class Transmission {
	 //文件名
   public String fileName;

   //文件长度
   public long fileLength;

   //传输类型
   public int transmissionType;

   //内容
   public String content;

   //已传输长度
   public long transLength;

   //0 文本  1  图片
   public int showType;

   // 私聊消息相关字段
   private String messageId;         // 消息唯一ID
   private String targetUserId;      // 目标用户ID
   private String messageType;       // 消息类型：text, image, file, voice
   private boolean isRead;          // 已读状态
   private long timestamp;          // 时间戳

   public Transmission() {
      this.messageId = UUID.randomUUID().toString().replace("-", "");
      this.timestamp = System.currentTimeMillis();
      this.isRead = false;
   }

   // 私聊消息构造方法
   public Transmission(String targetUserId, String messageType, String content, int transmissionType) {
      this();
      this.targetUserId = targetUserId;
      this.messageType = messageType;
      this.content = content;
      this.transmissionType = transmissionType;
   }

   // 文件消息构造方法
   public Transmission(String targetUserId, String messageType, String content,
                      String fileName, long fileLength, int transmissionType) {
      this(targetUserId, messageType, content, transmissionType);
      this.fileName = fileName;
      this.fileLength = fileLength;
   }

   // Getter和Setter方法
   public String getMessageId() {
      return messageId;
   }

   public void setMessageId(String messageId) {
      this.messageId = messageId;
   }

   public String getTargetUserId() {
      return targetUserId;
   }

   public void setTargetUserId(String targetUserId) {
      this.targetUserId = targetUserId;
   }

   public String getMessageType() {
      return messageType;
   }

   public void setMessageType(String messageType) {
      this.messageType = messageType;
   }

   public boolean isRead() {
      return isRead;
   }

   public void setRead(boolean read) {
      isRead = read;
   }

   public long getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
   }

   /**
    * 是否为私聊消息
    */
   public boolean isPrivateMessage() {
      return targetUserId != null && !targetUserId.trim().isEmpty();
   }

   /**
    * 获取消息类型的传输类型标识
    */
   public static int getMessageTransmissionType(String messageType) {
      switch (messageType) {
         case "text":
            return 0;  // 文本消息
         case "image":
            return 1;  // 图片消息
         case "file":
            return 2;  // 文件消息
         case "voice":
            return 3;  // 语音消息
         default:
            return 0;  // 默认文本
      }
   }

   /**
    * 根据传输类型获取消息类型
    */
   public static String getMessageTypeFromTransmission(int transmissionType) {
      switch (transmissionType) {
         case 0:
            return "text";
         case 1:
            return "image";
         case 2:
            return "file";
         case 3:
            return "voice";
         default:
            return "text";
      }
   }

   @Override
   public String toString() {
      return "Transmission{" +
              "fileName='" + fileName + '\'' +
              ", fileLength=" + fileLength +
              ", transmissionType=" + transmissionType +
              ", content='" + content + '\'' +
              ", transLength=" + transLength +
              ", showType=" + showType +
              ", messageId='" + messageId + '\'' +
              ", targetUserId='" + targetUserId + '\'' +
              ", messageType='" + messageType + '\'' +
              ", isRead=" + isRead +
              ", timestamp=" + timestamp +
              '}';
   }
}
