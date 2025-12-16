package Server_;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import DB.UserDB;
import Model.Friend;
import Model.FriendRequest;

/**
 * 服务器端好友管理器
 */
public class FriendManager {
    private UserDB userDB;
    private static FriendManager instance;

    // 缓存用户的好友列表，提高查询效率
    private Map<String, List<Friend>> friendListCache;
    private Map<String, List<FriendRequest>> requestCache;

    private FriendManager() {
        userDB = new UserDB("", "");
        friendListCache = new HashMap<>();
        requestCache = new HashMap<>();
    }

    public static FriendManager getInstance() {
        if (instance == null) {
            instance = new FriendManager();
        }
        return instance;
    }

    /**
     * 发送好友申请
     */
    public boolean sendFriendRequest(String fromUserId, String toUserId, String message) {
        System.out.println("处理好友申请: " + fromUserId + " -> " + toUserId);

        // 检查用户是否存在
        String[] toUserInfo = userDB.getUserInfo(toUserId);
        if (toUserInfo == null) {
            System.out.println("目标用户不存在: " + toUserId);
            return false;
        }

        // 发送申请
        boolean success = userDB.sendFriendRequest(fromUserId, toUserId, message);
        if (success) {
            System.out.println("好友申请发送成功");
            // 清除接收方的申请缓存
            requestCache.remove(toUserId);
        } else {
            System.out.println("好友申请发送失败，可能已经是好友或已发送申请");
        }
        return success;
    }

    /**
     * 处理好友申请（同意/拒绝）
     */
    public boolean processFriendRequest(int requestId, String fromUserId, String toUserId, int status) {
        System.out.println("处理好友申请: requestId=" + requestId + ", status=" + status);

        boolean success = userDB.processFriendRequest(requestId, fromUserId, toUserId, status);
        if (success) {
            System.out.println("好友申请处理成功");
            // 清除双方的好友列表和申请缓存
            friendListCache.remove(fromUserId);
            friendListCache.remove(toUserId);
            requestCache.remove(toUserId);

            if (status == 1) { // 同意申请
                System.out.println(fromUserId + " 和 " + toUserId + " 现在是好友了");
            }
        } else {
            System.out.println("好友申请处理失败");
        }
        return success;
    }

    /**
     * 获取用户的好友列表
     */
    public List<Friend> getFriendList(String userId) {
        // 先从缓存获取
        List<Friend> cachedList = friendListCache.get(userId);
        if (cachedList != null) {
            return new ArrayList<>(cachedList);
        }

        // 从数据库获取
        List<Friend> friendList = userDB.getFriendList(userId);

        // 更新缓存
        friendListCache.put(userId, new ArrayList<>(friendList));

        System.out.println("获取用户 " + userId + " 的好友列表，共 " + friendList.size() + " 个好友");
        return friendList;
    }

    /**
     * 获取待处理的好友申请
     */
    public List<FriendRequest> getPendingFriendRequests(String userId) {
        // 先从缓存获取
        List<FriendRequest> cachedRequests = requestCache.get(userId);
        if (cachedRequests != null) {
            return new ArrayList<>(cachedRequests);
        }

        // 从数据库获取
        List<FriendRequest> requests = userDB.getPendingFriendRequests(userId);

        // 更新缓存
        requestCache.put(userId, new ArrayList<>(requests));

        System.out.println("获取用户 " + userId + " 的待处理申请，共 " + requests.size() + " 个");
        return requests;
    }

    /**
     * 搜索用户
     */
    public List<String[]> searchUsers(String keyword, String currentUserId) {
        System.out.println("用户搜索: " + keyword + ", 搜索者: " + currentUserId);

        List<String[]> searchResults = userDB.searchUsers(keyword, currentUserId);
        System.out.println("搜索结果: " + searchResults.size() + " 个用户");

        return searchResults;
    }

    /**
     * 删除好友
     */
    public boolean deleteFriend(String userId, String friendId) {
        System.out.println("删除好友: " + userId + " -> " + friendId);

        boolean success = userDB.deleteFriend(userId, friendId);
        if (success) {
            System.out.println("删除好友成功");
            // 清除双方的好友列表缓存
            friendListCache.remove(userId);
            friendListCache.remove(friendId);
        } else {
            System.out.println("删除好友失败");
        }
        return success;
    }

    /**
     * 检查是否为好友关系
     */
    public boolean isFriend(String userId1, String userId2) {
        return userDB.isFriend(userId1, userId2);
    }

    /**
     * 获取好友列表的JSON格式字符串（用于客户端显示）
     */
    public String getFriendListJson(String userId) {
        List<Friend> friendList = getFriendList(userId);
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < friendList.size(); i++) {
            Friend friend = friendList.get(i);
            if (i > 0) json.append(",");
            json.append("{")
               .append("\"userId\":\"").append(friend.getFriendId()).append("\",")
               .append("\"nickname\":\"").append(friend.getFriendNickname() != null ? friend.getFriendNickname() : "").append("\",")
               .append("\"remark\":\"").append(friend.getFriendRemark() != null ? friend.getFriendRemark() : "").append("\",")
               .append("\"avatar\":\"").append(friend.getFriendAvatar() != null ? friend.getFriendAvatar() : "").append("\",")
               .append("\"online\":").append(friend.isOnline())
               .append("}");
        }

        json.append("]");
        return json.toString();
    }

    /**
     * 获取待处理申请的JSON格式字符串
     */
    public String getPendingRequestsJson(String userId) {
        List<FriendRequest> requests = getPendingFriendRequests(userId);
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < requests.size(); i++) {
            FriendRequest request = requests.get(i);
            if (i > 0) json.append(",");
            json.append("{")
               .append("\"requestId\":").append(request.getId()).append(",")
               .append("\"fromUserId\":\"").append(request.getFromUserId()).append("\",")
               .append("\"fromNickname\":\"").append(request.getFromUserNickname() != null ? request.getFromUserNickname() : "").append("\",")
               .append("\"fromAvatar\":\"").append(request.getFromUserAvatar() != null ? request.getFromUserAvatar() : "").append("\",")
               .append("\"message\":\"").append(request.getDisplayMessage()).append("\",")
               .append("\"createdTime\":\"").append(request.getCreatedTime() != null ? request.getCreatedTime().toString() : "").append("\"")
               .append("}");
        }

        json.append("]");
        return json.toString();
    }

    /**
     * 获取搜索结果的JSON格式字符串
     */
    public String getSearchResultsJson(String keyword, String currentUserId) {
        List<String[]> searchResults = searchUsers(keyword, currentUserId);
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < searchResults.size(); i++) {
            String[] userInfo = searchResults.get(i);
            if (i > 0) json.append(",");
            json.append("{")
               .append("\"userId\":\"").append(userInfo[0]).append("\",")
               .append("\"username\":\"").append(userInfo[1]).append("\",")
               .append("\"nickname\":\"").append(userInfo[2] != null ? userInfo[2] : "").append("\",")
               .append("\"avatar\":\"").append(userInfo[3] != null ? userInfo[3] : "").append("\",")
               .append("\"isFriend\":").append(isFriend(currentUserId, userInfo[0]))
               .append("}");
        }

        json.append("]");
        return json.toString();
    }

    /**
     * 更新用户在线状态
     */
    public void updateUserOnlineStatus(String userId, boolean isOnline) {
        List<Friend> friendList = getFriendList(userId);
        for (Friend friend : friendList) {
            friend.setOnline(isOnline);
        }

        // 同时更新作为好友时的状态
        for (List<Friend> list : friendListCache.values()) {
            for (Friend friend : list) {
                if (friend.getFriendId().equals(userId)) {
                    friend.setOnline(isOnline);
                }
            }
        }

        System.out.println("更新用户在线状态: " + userId + " -> " + (isOnline ? "在线" : "离线"));
    }

    /**
     * 清除用户缓存（用户下线时调用）
     */
    public void clearUserCache(String userId) {
        friendListCache.remove(userId);
        requestCache.remove(userId);
        System.out.println("清除用户缓存: " + userId);
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cachedFriendLists", friendListCache.size());
        stats.put("cachedRequests", requestCache.size());

        int totalFriends = 0;
        for (List<Friend> list : friendListCache.values()) {
            totalFriends += list.size();
        }
        stats.put("totalCachedFriends", totalFriends);

        return stats;
    }
}