package Model;

import java.sql.Timestamp;

/**
 * 聊天记录数据模型
 * 用于存储好友间的私聊消息记录
 */
public class ChatRecord {
    private Long id;
    private String fromUserId;      // 发送者用户ID
    private String toUserId;        // 接收者用户ID
    private String groupId;         // 群组ID（私聊时为null）
    private String messageType;     // 消息类型：text, image, file, voice, system
    private String content;         // 消息内容
    private String filePath;        // 文件路径
    private String fileName;        // 文件名
    private Long fileSize;          // 文件大小
    private Integer isRead;         // 是否已读：0=未读，1=已读
    private Timestamp createdTime;  // 创建时间

    // 构造方法
    public ChatRecord() {}

    public ChatRecord(String fromUserId, String toUserId, String messageType, String content) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.messageType = messageType;
        this.content = content;
        this.isRead = 0; // 默认未读
    }

    public ChatRecord(String fromUserId, String toUserId, String messageType, String content,
                     String filePath, String fileName, Long fileSize) {
        this(fromUserId, toUserId, messageType, content);
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * 是否为未读消息
     */
    public boolean isUnread() {
        return isRead != null && isRead == 0;
    }

    /**
     * 是否为已读消息
     */
    public boolean isRead() {
        return isRead != null && isRead == 1;
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = 1;
    }

    /**
     * 是否为私聊消息
     */
    public boolean isPrivateChat() {
        return groupId == null || groupId.trim().isEmpty();
    }

    /**
     * 是否为群聊消息
     */
    public boolean isGroupChat() {
        return !isPrivateChat();
    }

    /**
     * 是否为文件消息
     */
    public boolean isFileMessage() {
        return "file".equals(messageType);
    }

    /**
     * 是否为图片消息
     */
    public boolean isImageMessage() {
        return "image".equals(messageType);
    }

    /**
     * 是否为语音消息
     */
    public boolean isVoiceMessage() {
        return "voice".equals(messageType);
    }

    /**
     * 是否为系统消息
     */
    public boolean isSystemMessage() {
        return "system".equals(messageType);
    }

    /**
     * 是否为文本消息
     */
    public boolean isTextMessage() {
        return "text".equals(messageType);
    }

    /**
     * 获取显示的消息类型描述
     */
    public String getMessageTypeDisplay() {
        switch (messageType) {
            case "text":
                return "文本";
            case "image":
                return "图片";
            case "file":
                return "文件";
            case "voice":
                return "语音";
            case "system":
                return "系统";
            default:
                return "未知";
        }
    }

    /**
     * 获取格式化的文件大小
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize <= 0) {
            return "";
        }

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 获取简短的显示内容（用于列表显示）
     */
    public String getDisplayContent() {
        if (content == null || content.trim().isEmpty()) {
            if (isFileMessage()) {
                return "[文件] " + (fileName != null ? fileName : "未知文件");
            } else if (isImageMessage()) {
                return "[图片] " + (fileName != null ? fileName : "未知图片");
            } else if (isVoiceMessage()) {
                return "[语音] " + (fileName != null ? fileName : "语音消息");
            } else {
                return "[系统消息]";
            }
        }

        // 限制显示长度
        String displayContent = content.trim();
        if (displayContent.length() > 30) {
            displayContent = displayContent.substring(0, 27) + "...";
        }

        return displayContent;
    }

    @Override
    public String toString() {
        return "ChatRecord{" +
                "id=" + id +
                ", fromUserId='" + fromUserId + '\'' +
                ", toUserId='" + toUserId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", content='" + content + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", isRead=" + isRead +
                ", createdTime=" + createdTime +
                '}';
    }
}