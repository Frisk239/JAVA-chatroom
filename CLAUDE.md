# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于Java Socket的聊天室应用程序，包含客户端(Client)和服务器端(Server)组件。项目支持实时消息、文件传输（图片、语音）和用户注册登录功能。

## 构建和运行

### 开发环境要求
- Java 1.8
- MySQL数据库
- Eclipse/IntelliJ IDEA（项目包含两种IDE的配置文件）

### 编译项目
```bash
# 手动编译（如果使用命令行）
javac -cp "libs/*" -d bin src/Client/*.java src/Server_/*.java src/DB/*.java
```

### 运行应用
```bash
# 运行服务器
java -cp "bin;libs/*" Server_.Server

# 运行客户端
java -cp "bin;libs/*" Client.Client
```

### 数据库设置
- 使用 `check_db.sql` 脚本检查数据库结构
- 需要创建名为 `user` 的数据库
- 确保MySQL服务正在运行

## 项目架构

### 核心组件
- **Server_**: 服务器端核心组件
  - `Server.java`: 主服务器类，监听8888端口
  - `ServerThread.java`: 处理客户端连接的线程
  - `soctet_stream_map.java`: 用户到流的映射管理
  - `User.java`: 用户信息模型
  - `Transmission.java`: 传输协议处理
  - `Base64Utils.java`: Base64编解码工具

- **Client**: 客户端组件
  - `Client.java`: 主客户端界面和逻辑
  - `Login.java`: 登录界面
  - `Register.java`: 注册界面
  - `onLineWindow.java`: 在线用户窗口
  - `User.java`: 客户端用户模型
  - `Transmission.java`: 传输协议处理
  - `Base64Utils.java`: Base64编解码工具
  - `PlayWAV.java`: 音频播放功能

- **DB**: 数据库访问层
  - `UserDB.java`: 用户数据库操作

### 依赖库
- `commons-codec-1.8.jar`: Apache Commons Codec
- `gson-2.7.jar`: Google JSON处理库
- `mysql-connector-java-5.1.39-bin.jar`: MySQL JDBC驱动
- `substance.jar`: Swing外观主题库

### 通信架构
- **服务器**: 采用用户->流的映射模式，主线程阻塞等待连接，为每个客户端连接创建新的子线程
- **客户端**: 双线程设计（发送/接收线程）
- **文件传输**: 使用Base64编码，通过JSON打包传输，避免直接文件流传输导致的Socket关闭问题

## 端口配置
- 服务器端口: 8888（在 `Server.java:10` 中定义）

## 数据结构
- 用户信息存储在MySQL数据库的 `info` 表中
- 服务器使用 `soctet_stream_map<User, PrintStream>` 维护在线用户连接