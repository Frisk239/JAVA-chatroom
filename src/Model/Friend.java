package Model;

import java.sql.Timestamp;

/**
 * 好友关系数据模型
 */
public class Friend {
    private int id;
    private String userId;        // 用户ID
    private String friendId;      // 好友ID
    private int status;           // 好友状态：0=待确认，1=已确认，2=已拒绝，3=已删除
    private Timestamp createdTime;
    private Timestamp updatedTime;
    private String friendNickname; // 好友昵称
    private String friendRemark;   // 好友备注
    private String friendAvatar;   // 好友头像
    private boolean isOnline;      // 是否在线
    private String lastMessage;    // 最后一条消息
    private Timestamp lastMessageTime; // 最后消息时间

    // 构造方法
    public Friend() {}

    public Friend(String userId, String friendId, int status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }

    public Friend(int id, String userId, String friendId, int status,
                  Timestamp createdTime, Timestamp updatedTime) {
        this.id = id;
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getFriendNickname() {
        return friendNickname;
    }

    public void setFriendNickname(String friendNickname) {
        this.friendNickname = friendNickname;
    }

    public String getFriendRemark() {
        return friendRemark;
    }

    public void setFriendRemark(String friendRemark) {
        this.friendRemark = friendRemark;
    }

    public String getFriendAvatar() {
        return friendAvatar;
    }

    public void setFriendAvatar(String friendAvatar) {
        this.friendAvatar = friendAvatar;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Timestamp getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Timestamp lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        switch (status) {
            case 0:
                return "待确认";
            case 1:
                return "已确认";
            case 2:
                return "已拒绝";
            case 3:
                return "已删除";
            default:
                return "未知状态";
        }
    }

    /**
     * 是否为有效好友关系
     */
    public boolean isActiveFriend() {
        return status == 1;
    }

    /**
     * 是否为待处理状态
     */
    public boolean isPending() {
        return status == 0;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", friendId='" + friendId + '\'' +
                ", status=" + status +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                ", friendNickname='" + friendNickname + '\'' +
                ", friendRemark='" + friendRemark + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Friend friend = (Friend) obj;
        return userId.equals(friend.userId) && friendId.equals(friend.friendId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode() + friendId.hashCode();
    }
}