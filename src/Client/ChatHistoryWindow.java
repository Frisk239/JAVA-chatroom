package Client;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import Model.Friend;
import Model.ChatRecord;
import DB.UserDB;

/**
 * 聊天记录查看窗口
 */
public class ChatHistoryWindow extends JDialog {
    private static final long serialVersionUID = 1L;

    // UI组件
    private JPanel contentPane;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JLabel titleLabel;
    private JButton clearButton;
    private JButton closeButton;

    // 数据
    private PrivateChatWindow parentWindow;
    private Friend friend;
    private String currentUserId;
    private UserDB userDB;
    private List<ChatRecord> chatHistory;

    public ChatHistoryWindow(PrivateChatWindow parentWindow, Friend friend, String currentUserId) {
        super(parentWindow, "聊天记录", true);
        this.parentWindow = parentWindow;
        this.friend = friend;
        this.currentUserId = currentUserId;
        this.userDB = new UserDB("", "");

        initializeUI();
        loadChatHistory();
    }

    /**
     * 初始化UI
     */
    private void initializeUI() {
        setBounds(100, 100, 800, 600);
        setLocationRelativeTo(parentWindow);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        // 顶部标题栏
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(70, 130, 180));
        topPanel.setPreferredSize(new Dimension(0, 50));
        topPanel.setLayout(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        titleLabel = new JLabel("与 " + friend.getFriendNickname() + " 的聊天记录");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        clearButton = new JButton("清空记录");
        clearButton.setBackground(new Color(255, 87, 87));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setBorderPainted(false);
        clearButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        closeButton = new JButton("关闭");
        closeButton.setBackground(new Color(70, 130, 180));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);

        topPanel.add(buttonPanel, BorderLayout.EAST);
        contentPane.add(topPanel, BorderLayout.NORTH);

        // 创建表格
        createHistoryTable();

        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        // 事件监听器
        setupEventListeners();
    }

    /**
     * 创建聊天记录表格
     */
    private void createHistoryTable() {
        // 表格列定义
        String[] columnNames = {"时间", "发送者", "消息类型", "内容", "状态", "文件名"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格都不可编辑
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        historyTable.setRowHeight(25);
        historyTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(70, 130, 180));
        historyTable.getTableHeader().setForeground(Color.WHITE);

        // 设置列宽
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150); // 时间
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(100); // 发送者
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 消息类型
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(300); // 内容
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 状态
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(150); // 文件名

        // 行选择器
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setSelectionBackground(new Color(51, 153, 255));
        historyTable.setSelectionForeground(Color.WHITE);

        scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 清空记录按钮
        clearButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要清空与 " + friend.getFriendNickname() + " 的所有聊天记录吗？\n此操作不可恢复！",
                "确认清空",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                clearChatHistory();
            }
        });

        // 关闭按钮
        closeButton.addActionListener(e -> dispose());

        // 双击表格行查看详情
        historyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = historyTable.getSelectedRow();
                    if (selectedRow >= 0 && selectedRow < chatHistory.size()) {
                        ChatRecord record = chatHistory.get(selectedRow);
                        showRecordDetails(record);
                    }
                }
            }
        });

        // 表格行选择事件
        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = historyTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < chatHistory.size()) {
                    ChatRecord record = chatHistory.get(selectedRow);
                    updateButtonStates();
                }
            }
        });
    }

    /**
     * 加载聊天记录
     */
    private void loadChatHistory() {
        try {
            // 获取所有聊天记录
            chatHistory = userDB.getFriendChatHistory(currentUserId, friend.getFriendId(), 1000);

            // 清空表格
            while (tableModel.getRowCount() > 0) {
                tableModel.removeRow(0);
            }

            // 添加记录到表格
            for (ChatRecord record : chatHistory) {
                Object[] rowData = {
                        formatTime(record.getCreatedTime()),
                        getSenderName(record.getFromUserId()),
                        record.getMessageTypeDisplay(),
                        record.getDisplayContent(),
                        record.isRead() ? "已读" : "未读",
                        record.getFileName() != null ? record.getFileName() : ""
                };
                tableModel.addRow(rowData);
            }

            updateButtonStates();
            titleLabel.setText("与 " + friend.getFriendNickname() + " 的聊天记录 (" + chatHistory.size() + " 条)");

        } catch (Exception e) {
            System.out.println("加载聊天记录失败: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "加载聊天记录失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 清空聊天记录
     */
    private void clearChatHistory() {
        try {
            // 清空用户的所有聊天记录
            boolean success = userDB.clearAllChatRecords(currentUserId);

            if (success) {
                // 清空表格
                while (tableModel.getRowCount() > 0) {
                    tableModel.removeRow(0);
                }

                // 清空内存列表
                chatHistory.clear();

                titleLabel.setText("与 " + friend.getFriendNickname() + " 的聊天记录 (0 条)");
                JOptionPane.showMessageDialog(this, "聊天记录已清空", "成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "清空聊天记录失败", "错误", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            System.out.println("清空聊天记录失败: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "清空聊天记录失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        int selectedRow = historyTable.getSelectedRow();
        boolean hasSelection = selectedRow >= 0 && selectedRow < chatHistory.size();

        // 如果是文件消息，启用相关操作按钮
        if (hasSelection) {
            ChatRecord record = chatHistory.get(selectedRow);
            boolean isFile = record.isFileMessage() || record.isImageMessage();
            // 这里可以添加文件相关的按钮操作
        }
    }

    /**
     * 显示记录详情
     */
    private void showRecordDetails(ChatRecord record) {
        StringBuilder details = new StringBuilder();
        details.append("消息详情\n\n");
        details.append("发送者: ").append(getSenderName(record.getFromUserId())).append("\n");
        details.append("接收者: ").append(friend.getFriendNickname()).append("\n");
        details.append("消息类型: ").append(record.getMessageTypeDisplay()).append("\n");
        details.append("发送时间: ").append(formatTime(record.getCreatedTime())).append("\n");
        details.append("读取状态: ").append(record.isRead() ? "已读" : "未读").append("\n");

        if (record.isFileMessage()) {
            details.append("文件名: ").append(record.getFileName()).append("\n");
            details.append("文件大小: ").append(record.getFormattedFileSize()).append("\n");
            details.append("文件路径: ").append(record.getFilePath() != null ? record.getFilePath() : "").append("\n");
        }

        if (record.getContent() != null && !record.getContent().isEmpty()) {
            details.append("\n消息内容:\n").append(record.getContent());
        }

        JOptionPane.showMessageDialog(this, details.toString(), "消息详情", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 获取发送者名称
     */
    private String getSenderName(String userId) {
        if (currentUserId.equals(userId)) {
            return "我";
        } else {
            return friend.getFriendNickname();
        }
    }

    /**
     * 格式化时间显示
     */
    private String formatTime(java.sql.Timestamp time) {
        if (time == null) {
            return "";
        }
        return time.toString().substring(0, 19); // 去掉毫秒部分
    }

    /**
     * 刷新表格显示
     */
    public void refreshHistory() {
        loadChatHistory();
    }
}