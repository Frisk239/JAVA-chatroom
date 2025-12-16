package Model;

import java.sql.Timestamp;

import com.google.gson.annotations.SerializedName;

/**
 * 好友申请数据模型
 */
public class FriendRequest {
    @SerializedName("requestId")
    private int id;
    private String fromUserId;    // 申请发起者用户ID
    private String toUserId;      // 申请接收者用户ID
    private String message;       // 申请消息
    private int status;           // 状态：0=待处理，1=已同意，2=已拒绝
    private String createdTime;  // 改为String类型，避免Gson时间戳解析问题
    private String processedTime; // 改为String类型，避免Gson时间戳解析问题
    @SerializedName("fromNickname")
    private String fromUserNickname;  // 申请人昵称
    private String toUserNickname;    // 接收人昵称
    @SerializedName("fromAvatar")
    private String fromUserAvatar;    // 申请人头像

    // 构造方法
    public FriendRequest() {}

    public FriendRequest(String fromUserId, String toUserId, String message, int status) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.message = message;
        this.status = status;
    }

    public FriendRequest(int id, String fromUserId, String toUserId, String message,
                         int status, String createdTime, String processedTime) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.message = message;
        this.status = status;
        this.createdTime = createdTime;
        this.processedTime = processedTime;
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(String processedTime) {
        this.processedTime = processedTime;
    }

    public String getFromUserNickname() {
        return fromUserNickname;
    }

    public void setFromUserNickname(String fromUserNickname) {
        this.fromUserNickname = fromUserNickname;
    }

    public String getToUserNickname() {
        return toUserNickname;
    }

    public void setToUserNickname(String toUserNickname) {
        this.toUserNickname = toUserNickname;
    }

    public String getFromUserAvatar() {
        return fromUserAvatar;
    }

    public void setFromUserAvatar(String fromUserAvatar) {
        this.fromUserAvatar = fromUserAvatar;
    }

    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        switch (status) {
            case 0:
                return "待处理";
            case 1:
                return "已同意";
            case 2:
                return "已拒绝";
            default:
                return "未知状态";
        }
    }

    /**
     * 是否为待处理状态
     */
    public boolean isPending() {
        return status == 0;
    }

    /**
     * 是否已处理
     */
    public boolean isProcessed() {
        return status == 1 || status == 2;
    }

    /**
     * 是否已同意
     */
    public boolean isAccepted() {
        return status == 1;
    }

    /**
     * 是否已拒绝
     */
    public boolean isRejected() {
        return status == 2;
    }

    /**
     * 获取申请消息的显示文本（如果消息为空，显示默认消息）
     */
    public String getDisplayMessage() {
        if (message == null || message.trim().isEmpty()) {
            return "请求添加您为好友";
        }
        return message;
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "id=" + id +
                ", fromUserId='" + fromUserId + '\'' +
                ", toUserId='" + toUserId + '\'' +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", createdTime=" + createdTime +
                ", processedTime=" + processedTime +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        FriendRequest that = (FriendRequest) obj;
        return fromUserId.equals(that.fromUserId) && toUserId.equals(that.toUserId);
    }

    @Override
    public int hashCode() {
        return fromUserId.hashCode() + toUserId.hashCode();
    }
}