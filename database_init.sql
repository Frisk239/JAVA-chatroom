-- Java聊天室数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS chatroom DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE chatroom;

-- 1. 用户信息表（扩展原有info表）
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) UNIQUE NOT NULL COMMENT '8位用户账号',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（MD5加密）',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
    status TINYINT DEFAULT 1 COMMENT '状态：1=正常，0=禁用',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 2. 好友关系表
DROP TABLE IF EXISTS friends;
CREATE TABLE friends (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL COMMENT '发起好友关系的用户ID',
    friend_id VARCHAR(20) NOT NULL COMMENT '好友用户ID',
    status TINYINT DEFAULT 0 COMMENT '好友状态：0=待确认，1=已确认，2=已拒绝，3=已删除',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_friend (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_friend_id (friend_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 3. 好友申请记录表
DROP TABLE IF EXISTS friend_requests;
CREATE TABLE friend_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    from_user_id VARCHAR(20) NOT NULL COMMENT '申请发起者用户ID',
    to_user_id VARCHAR(20) NOT NULL COMMENT '申请接收者用户ID',
    message TEXT DEFAULT NULL COMMENT '申请消息',
    status TINYINT DEFAULT 0 COMMENT '状态：0=待处理，1=已同意，2=已拒绝',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_time TIMESTAMP NULL,
    FOREIGN KEY (from_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (to_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友申请记录表';

-- 4. 群组表
DROP TABLE IF EXISTS groups;
CREATE TABLE groups (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id VARCHAR(20) UNIQUE NOT NULL COMMENT '群组ID',
    group_name VARCHAR(100) NOT NULL COMMENT '群组名称',
    group_description TEXT DEFAULT NULL COMMENT '群组描述',
    owner_id VARCHAR(20) NOT NULL COMMENT '群主用户ID',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '群头像路径',
    max_members INT DEFAULT 200 COMMENT '最大成员数',
    status TINYINT DEFAULT 1 COMMENT '状态：1=正常，0=解散',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_group_id (group_id),
    INDEX idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组表';

-- 5. 群组成员表
DROP TABLE IF EXISTS group_members;
CREATE TABLE group_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id VARCHAR(20) NOT NULL COMMENT '群组ID',
    user_id VARCHAR(20) NOT NULL COMMENT '用户ID',
    role TINYINT DEFAULT 1 COMMENT '角色：1=普通成员，2=管理员，3=群主',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '群内昵称',
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1=正常，0=已退出，2=被踢出',
    UNIQUE KEY uk_group_user (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_group_id (group_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组成员表';

-- 6. 聊天记录表
DROP TABLE IF EXISTS chat_records;
CREATE TABLE chat_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_user_id VARCHAR(20) NOT NULL COMMENT '发送者用户ID',
    to_user_id VARCHAR(20) DEFAULT NULL COMMENT '接收者用户ID（私聊时使用）',
    group_id VARCHAR(20) DEFAULT NULL COMMENT '群组ID（群聊时使用）',
    message_type VARCHAR(20) DEFAULT 'text' COMMENT '消息类型：text, image, file, voice, system',
    content TEXT NOT NULL COMMENT '消息内容',
    file_path VARCHAR(255) DEFAULT NULL COMMENT '文件路径',
    file_name VARCHAR(255) DEFAULT NULL COMMENT '文件名',
    file_size BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (to_user_id) REFERENCES users(user_id) ON DELETE CASCADE SET NULL,
    FOREIGN KEY (group_id) REFERENCES groups(group_id) ON DELETE CASCADE SET NULL,
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_group_id (group_id),
    INDEX idx_created_time (created_time),
    INDEX idx_message_type (message_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天记录表';

-- 7. 安全问题表（用于密码找回）
DROP TABLE IF EXISTS security_questions;
CREATE TABLE security_questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL COMMENT '用户ID',
    question VARCHAR(255) NOT NULL COMMENT '安全问题',
    answer VARCHAR(255) NOT NULL COMMENT '安全问题答案',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全问题表';

-- 8. 系统消息表
DROP TABLE IF EXISTS system_messages;
CREATE TABLE system_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message_type VARCHAR(50) NOT NULL COMMENT '消息类型：announcement, notification',
    title VARCHAR(255) NOT NULL COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息内容',
    target_type VARCHAR(20) DEFAULT 'all' COMMENT '目标类型：all, group, user',
    target_id VARCHAR(50) DEFAULT NULL COMMENT '目标ID（群组ID或用户ID）',
    sender_id VARCHAR(20) DEFAULT 'system' COMMENT '发送者ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1=有效，0=无效',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_message_type (message_type),
    INDEX idx_target_type (target_type),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统消息表';

-- 插入默认群主（用于创建系统群组）
INSERT INTO users (user_id, username, password, nickname, status) VALUES
('10000000', 'admin', '21232f297a57a5a743894a0e4a801fc3', '系统管理员', 1);

-- 创建初始默认群组
INSERT INTO groups (group_id, group_name, group_description, owner_id, status) VALUES
('G0000001', '系统通知群', '用于系统消息通知的默认群组', '10000000', 1);

-- 将管理员加入群组
INSERT INTO group_members (group_id, user_id, role, nickname, status) VALUES
('G0000001', '10000000', 3, '系统管理员', 1);

-- 插入一些示例安全问题选项
-- 注意：这些是预定义的问题模板，实际用户设置时会选择并自定义答案

-- 创建索引优化查询性能
CREATE INDEX idx_chat_records_time_user ON chat_records(created_time, from_user_id);
CREATE INDEX idx_friends_status ON friends(user_id, status);
CREATE INDEX idx_group_members_active ON group_members(group_id, status);

-- 显示创建的表
SHOW TABLES;

-- 添加一些说明
SELECT '数据库初始化完成！' as message;
SELECT '请查看数据库结构，确保所有表已正确创建' as note;