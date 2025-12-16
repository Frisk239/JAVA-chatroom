package Model;

/**
 * 搜索结果数据模型
 */
public class SearchResult {
    private String userId;
    private String username;
    private String nickname;
    private String avatar;
    private boolean isFriend;

    public SearchResult() {}

    public SearchResult(String userId, String username, String nickname, String avatar, boolean isFriend) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.isFriend = isFriend;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    /**
     * 转换为字符串数组格式，用于现有的显示逻辑
     */
    public String[] toStringArray() {
        return new String[]{
            userId != null ? userId : "",
            username != null ? username : "",
            nickname != null ? nickname : "",
            avatar != null ? avatar : "",
            String.valueOf(isFriend)
        };
    }
}