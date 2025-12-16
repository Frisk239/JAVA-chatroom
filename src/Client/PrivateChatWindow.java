package Client;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import Model.Friend;
import Model.ChatRecord;
import DB.UserDB;

/**
 * 好友私聊窗口
 */
public class PrivateChatWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    // UI组件
    private JPanel contentPane;
    private JTextPane chatArea;           // 聊天显示区域
    private JTextField inputField;        // 输入框
    private JButton sendButton;          // 发送按钮
    private JButton fileButton;          // 文件按钮
    private JButton imageButton;         // 图片按钮
    private JButton voiceButton;         // 语音按钮
    private JButton historyButton;       // 聊天记录按钮
    private JLabel friendInfoLabel;      // 好友信息标签
    private JLabel unreadCountLabel;    // 未读消息数量标签
    private JScrollPane chatScrollPane;  // 聊天区域滚动

    // 数据
    private Client parentClient;
    private Friend friend;               // 当前聊天的好友
    private String currentUserId;        // 当前用户ID
    private List<ChatRecord> chatHistory; // 聊天历史
    private UserDB userDB;              // 数据库操作对象

    // 窗口管理
    private static java.util.Map<String, PrivateChatWindow> openWindows = new java.util.HashMap<>();

    public PrivateChatWindow(Client parentClient, Friend friend, String currentUserId) {
        this.parentClient = parentClient;
        this.friend = friend;
        this.currentUserId = currentUserId;
        this.userDB = new UserDB("", "");

        initializeUI();
        loadChatHistory();

        // 将此窗口添加到窗口管理器
        openWindows.put(friend.getFriendId(), this);

        // 添加窗口关闭事件监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                markMessagesAsRead();
                openWindows.remove(friend.getFriendId());
            }
        });
    }

    /**
     * 获取已打开的私聊窗口
     */
    public static PrivateChatWindow getOpenWindow(String friendId) {
        return openWindows.get(friendId);
    }

    /**
     * 检查是否有窗口打开
     */
    public static boolean hasOpenWindow(String friendId) {
        return openWindows.containsKey(friendId);
    }

    /**
     * 获取所有打开的私聊窗口
     */
    public static java.util.Map<String, PrivateChatWindow> getAllOpenWindows() {
        return new java.util.HashMap<>(openWindows);
    }

    /**
     * 初始化UI
     */
    private void initializeUI() {
        setTitle("与 " + friend.getFriendNickname() + " 私聊");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 600, 500);
        setLocationRelativeTo(null);
        setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        // 顶部好友信息面板
        createTopPanel();

        // 聊天区域
        createChatArea();

        // 底部输入区域
        createBottomPanel();

        // 事件监听器
        setupEventListeners();

        // 更新好友信息显示
        updateFriendInfo();
    }

    /**
     * 创建顶部好友信息面板
     */
    private void createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(70, 130, 180));
        topPanel.setPreferredSize(new Dimension(0, 70));
        topPanel.setLayout(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        contentPane.add(topPanel, BorderLayout.NORTH);

        // 左侧好友信息
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        // 在线状态指示器
        JLabel statusLabel = new JLabel();
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        if (friend.isOnline()) {
            statusLabel.setBackground(new Color(76, 175, 80)); // 绿色
        } else {
            statusLabel.setBackground(new Color(158, 158, 158)); // 灰色
        }
        statusLabel.setPreferredSize(new Dimension(12, 12));
        leftPanel.add(statusLabel);

        // 好友信息
        friendInfoLabel = new JLabel(friend.getFriendNickname() + " (" +
                                   (friend.isOnline() ? "在线" : "离线") + ")");
        friendInfoLabel.setForeground(Color.WHITE);
        friendInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        leftPanel.add(friendInfoLabel);

        // 中间未读消息数量
        unreadCountLabel = new JLabel();
        unreadCountLabel.setForeground(Color.WHITE);
        unreadCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        unreadCountLabel.setVisible(false); // 默认隐藏

        // 右侧功能按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);

        historyButton = new JButton("聊天记录");
        historyButton.setBackground(new Color(255, 255, 255, 200));
        historyButton.setForeground(new Color(70, 130, 180));
        historyButton.setFocusPainted(false);
        historyButton.setBorderPainted(false);
        historyButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        rightPanel.add(historyButton);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(unreadCountLabel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);
    }

    /**
     * 创建聊天区域
     */
    private void createChatArea() {
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(Color.WHITE);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 设置样式文档
        StyledDocument doc = chatArea.getStyledDocument();
        StyleContext context = new StyleContext();

        // 默认样式
        Style defaultStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(defaultStyle, "微软雅黑");
        StyleConstants.setFontSize(defaultStyle, 14);
        doc.addStyle("default", defaultStyle);

        // 自己的消息样式
        Style selfStyle = context.addStyle("self", defaultStyle);
        StyleConstants.setBackground(selfStyle, new Color(220, 237, 255));
        StyleConstants.setAlignment(selfStyle, StyleConstants.ALIGN_RIGHT);

        // 对方消息样式
        Style otherStyle = context.addStyle("other", defaultStyle);
        StyleConstants.setBackground(otherStyle, new Color(240, 240, 240));

        // 时间戳样式
        Style timeStyle = context.addStyle("time", defaultStyle);
        StyleConstants.setForeground(timeStyle, Color.GRAY);
        StyleConstants.setFontSize(timeStyle, 12);
        StyleConstants.setAlignment(timeStyle, StyleConstants.ALIGN_CENTER);

        chatArea.setLogicalStyle(defaultStyle);

        chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        contentPane.add(chatScrollPane, BorderLayout.CENTER);
    }

    /**
     * 创建底部输入区域
     */
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(0, 5));
        bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        bottomPanel.setBackground(new Color(245, 245, 245));
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        // 功能按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setOpaque(false);

        fileButton = new JButton("📄 文件");
        fileButton.setBackground(Color.WHITE);
        fileButton.setForeground(Color.BLACK);
        fileButton.setFocusPainted(false);
        fileButton.setBorderPainted(false);
        fileButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        fileButton.setToolTipText("发送文件");

        imageButton = new JButton("🖼️ 图片");
        imageButton.setBackground(Color.WHITE);
        imageButton.setForeground(Color.BLACK);
        imageButton.setFocusPainted(false);
        imageButton.setBorderPainted(false);
        imageButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        imageButton.setToolTipText("发送图片");

        voiceButton = new JButton("🎤 语音");
        voiceButton.setBackground(Color.WHITE);
        voiceButton.setForeground(Color.BLACK);
        voiceButton.setFocusPainted(false);
        voiceButton.setBorderPainted(false);
        voiceButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        voiceButton.setToolTipText("发送语音");

        buttonPanel.add(fileButton);
        buttonPanel.add(imageButton);
        buttonPanel.add(voiceButton);

        // 输入框和发送按钮
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout(5, 0));
        inputPanel.setOpaque(false);

        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        inputField.setBackground(Color.WHITE);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)
        ));

        sendButton = new JButton("发送");
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        sendButton.setPreferredSize(new Dimension(80, 35));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
    }

    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 发送按钮事件
        sendButton.addActionListener(e -> sendMessage());

        // 输入框回车发送
        inputField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        // 聊天记录按钮事件
        historyButton.addActionListener(e -> showChatHistory());

        // 文件按钮事件
        fileButton.addActionListener(e -> sendFile());

        // 图片按钮事件
        imageButton.addActionListener(e -> sendImage());

        // 语音按钮事件
        voiceButton.addActionListener(e -> sendVoice());

        // 窗口焦点事件 - 标记消息为已读
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                markMessagesAsRead();
            }
        });
    }

    /**
     * 发送消息
     */
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        // 通过父客户端发送私聊消息
        if (parentClient != null) {
            parentClient.sendPrivateMessage(friend.getFriendId(), message, "text");

            // 立即在本地显示发送的消息
            appendMessage(currentUserId, message, "text", true);
        }

        // 清空输入框
        inputField.setText("");
        inputField.requestFocus();
    }

    /**
     * 接收消息
     */
    public void receiveMessage(String fromUserId, String content, String messageType) {
        SwingUtilities.invokeLater(() -> {
            appendMessage(fromUserId, content, messageType, false);

            // 如果窗口不是活动状态，更新未读消息数
            if (!isActive()) {
                updateUnreadCount();
            }

            // 播放提示音
            playNotificationSound();
        });
    }

    /**
     * 显示消息在聊天区域
     */
    private void appendMessage(String fromUserId, String content, String messageType, boolean isSelf) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();

            // 添加时间戳（每10条消息显示一次）
            if (chatHistory.size() % 10 == 0) {
                insertTimeSeparator(doc);
            }

            // 根据发送者选择样式
            String style = isSelf ? "self" : "other";

            // 创建消息内容
            String displayContent = content;
            if ("image".equals(messageType)) {
                displayContent = "[图片]";
            } else if ("file".equals(messageType)) {
                displayContent = "[文件]";
            } else if ("voice".equals(messageType)) {
                displayContent = "[语音]";
            }

            // 插入消息
            SimpleAttributeSet attrSet = new SimpleAttributeSet();
            StyleConstants.setAlignment(attrSet,
                isSelf ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);

            // 添加发送者信息（如果不是自己的消息）
            if (!isSelf) {
                String senderInfo = friend.getFriendNickname();
                SimpleAttributeSet senderAttr = new SimpleAttributeSet();
                StyleConstants.setForeground(senderAttr, Color.BLUE);
                StyleConstants.setFontSize(senderAttr, 12);

                doc.insertString(doc.getLength(), senderInfo + ": ", senderAttr);
            }

            // 插入消息内容
            doc.insertString(doc.getLength(), displayContent, attrSet);

            // 添加换行
            doc.insertString(doc.getLength(), "\n", attrSet);

            // 滚动到底部
            chatArea.setCaretPosition(doc.getLength());

        } catch (Exception e) {
            System.out.println("显示消息失败: " + e.getMessage());
        }
    }

    /**
     * 插入时间分隔符
     */
    private void insertTimeSeparator(StyledDocument doc) {
        try {
            String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            SimpleAttributeSet timeAttr = new SimpleAttributeSet();
            StyleConstants.setAlignment(timeAttr, StyleConstants.ALIGN_CENTER);

            doc.insertString(doc.getLength(), "---------- " + time + " ----------", timeAttr);
            doc.insertString(doc.getLength(), "\n\n", timeAttr);
        } catch (Exception e) {
            System.out.println("插入时间戳失败: " + e.getMessage());
        }
    }

    /**
     * 更新好友信息显示
     */
    private void updateFriendInfo() {
        String status = friend.isOnline() ? "在线" : "离线";
        friendInfoLabel.setText(friend.getFriendNickname() + " (" + status + ")");

        // 更新在线状态指示器颜色
        Component[] components = ((JPanel)friendInfoLabel.getParent()).getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel && comp != friendInfoLabel) {
                if (friend.isOnline()) {
                    comp.setBackground(new Color(76, 175, 80)); // 绿色
                } else {
                    comp.setBackground(new Color(158, 158, 158)); // 灰色
                }
                break;
            }
        }
    }

    /**
     * 更新未读消息数量
     */
    private void updateUnreadCount() {
        int unreadCount = userDB.getUnreadMessageCountFromFriend(friend.getFriendId(), currentUserId);
        if (unreadCount > 0) {
            unreadCountLabel.setText("未读: " + unreadCount);
            unreadCountLabel.setVisible(true);
        } else {
            unreadCountLabel.setVisible(false);
        }
    }

    /**
     * 标记消息为已读
     */
    private void markMessagesAsRead() {
        if (userDB.markMessagesAsRead(friend.getFriendId(), currentUserId)) {
            unreadCountLabel.setVisible(false);
        }
    }

    /**
     * 播放提示音
     */
    private void playNotificationSound() {
        try {
            if (parentClient != null) {
                parentClient.playWAV.Play("notification.wav");
            }
        } catch (Exception e) {
            System.out.println("播放提示音失败: " + e.getMessage());
        }
    }

    /**
     * 发送文件
     */
    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (parentClient != null) {
                parentClient.sendPrivateFile(friend.getFriendId(), selectedFile);

                // 在本地显示文件发送消息
                appendMessage(currentUserId, "[文件] " + selectedFile.getName(), "file", true);
            }
        }
    }

    /**
     * 发送图片
     */
    private void sendImage() {
        JFileChooser imageChooser = new JFileChooser();
        imageChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "图片文件", "jpg", "jpeg", "png", "gif", "bmp"));

        if (imageChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = imageChooser.getSelectedFile();
            if (parentClient != null) {
                parentClient.sendPrivateImage(friend.getFriendId(), selectedFile);

                // 在本地显示图片发送消息
                appendMessage(currentUserId, "[图片] " + selectedFile.getName(), "image", true);
            }
        }
    }

    /**
     * 发送语音
     */
    private void sendVoice() {
        JOptionPane.showMessageDialog(this, "语音功能开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示聊天记录
     */
    private void showChatHistory() {
        // 创建聊天记录窗口
        ChatHistoryWindow historyWindow = new ChatHistoryWindow(this, friend, currentUserId);
        historyWindow.setVisible(true);
    }

    /**
     * 加载聊天历史
     */
    private void loadChatHistory() {
        try {
            // 获取最近的50条聊天记录
            chatHistory = userDB.getFriendChatHistory(currentUserId, friend.getFriendId(), 50);

            // 按时间顺序显示（从旧到新）
            for (int i = chatHistory.size() - 1; i >= 0; i--) {
                ChatRecord record = chatHistory.get(i);
                boolean isSelf = currentUserId.equals(record.getFromUserId());
                String messageType = record.getMessageType();
                String content = record.getContent();

                if (content == null) {
                    continue;
                }

                // 在聊天区域显示历史消息
                if ("image".equals(messageType)) {
                    content = "[图片]";
                } else if ("file".equals(messageType)) {
                    content = "[文件]";
                } else if ("voice".equals(messageType)) {
                    content = "[语音]";
                }

                appendMessage(record.getFromUserId(), content, messageType, isSelf);
            }

            // 滚动到最底部
            SwingUtilities.invokeLater(() -> {
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            });

        } catch (Exception e) {
            System.out.println("加载聊天历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取好友信息
     */
    public Friend getFriend() {
        return friend;
    }

    /**
     * 获取当前用户ID
     */
    public String getCurrentUserId() {
        return currentUserId;
    }
}