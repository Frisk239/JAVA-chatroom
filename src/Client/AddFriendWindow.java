package Client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import Model.SearchResult;

/**
 * 添加好友窗口
 */
public class AddFriendWindow extends JDialog {
    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JTextField txtSearchKeyword;
    private JTextArea txtSearchResults;
    private JButton btnSearch;
    private JButton btnSendRequest;
    private JButton btnCancel;
    private Client parentClient;
    private FriendListWindow parentFriendList;
    private JList<String> resultList;
    private DefaultListModel<String> listModel;

    // 搜索结果数据
    private List<String[]> searchResults;

    public AddFriendWindow(Client parentClient, FriendListWindow parentFriendList) {
        super(parentFriendList, "添加好友", true);
        this.parentClient = parentClient;
        this.parentFriendList = parentFriendList;

        initializeUI();
    }

    private void initializeUI() {
        setBounds(100, 100, 400, 500);
        setLocationRelativeTo(parentFriendList);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(255, 182, 193));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // 标题
        JLabel titleLabel = new JLabel("添加好友");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setBounds(15, 10, 100, 30);
        contentPane.add(titleLabel);

        // 搜索框标签
        JLabel lblSearch = new JLabel("搜索用户:");
        lblSearch.setForeground(Color.WHITE);
        lblSearch.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblSearch.setBounds(20, 50, 80, 25);
        contentPane.add(lblSearch);

        // 搜索输入框
        txtSearchKeyword = new JTextField();
        txtSearchKeyword.setBounds(100, 50, 200, 30);
        txtSearchKeyword.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtSearchKeyword.setBorder(BorderFactory.createLineBorder(new Color(255, 140, 160), 1));
        contentPane.add(txtSearchKeyword);
        txtSearchKeyword.setColumns(10);

        // 搜索按钮
        btnSearch = new JButton("搜索");
        btnSearch.setBackground(new Color(255, 140, 160));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setBounds(310, 50, 60, 30);
        btnSearch.addActionListener(e -> searchUsers());
        contentPane.add(btnSearch);

        // 搜索结果
        JLabel lblResults = new JLabel("搜索结果:");
        lblResults.setForeground(Color.WHITE);
        lblResults.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblResults.setBounds(20, 90, 80, 25);
        contentPane.add(lblResults);

        // 搜索结果列表
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(20, 120, 350, 250);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 140, 160), 1));
        contentPane.add(scrollPane);

        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        resultList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        resultList.setBackground(Color.WHITE);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setCellRenderer(new SearchResultCellRenderer());
        scrollPane.setViewportView(resultList);

        // 申请消息输入框
        JLabel lblMessage = new JLabel("申请消息:");
        lblMessage.setForeground(Color.WHITE);
        lblMessage.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblMessage.setBounds(20, 380, 80, 25);
        contentPane.add(lblMessage);

        txtSearchResults = new JTextArea();
        txtSearchResults.setBounds(100, 380, 270, 50);
        txtSearchResults.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        txtSearchResults.setText("请求添加您为好友");
        txtSearchResults.setBorder(BorderFactory.createLineBorder(new Color(255, 140, 160), 1));
        txtSearchResults.setLineWrap(true);
        txtSearchResults.setWrapStyleWord(true);
        contentPane.add(txtSearchResults);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 182, 193));
        buttonPanel.setBounds(20, 440, 350, 40);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        contentPane.add(buttonPanel);

        btnSendRequest = new JButton("发送申请");
        btnSendRequest.setBackground(new Color(255, 140, 160));
        btnSendRequest.setForeground(Color.WHITE);
        btnSendRequest.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnSendRequest.setFocusPainted(false);
        btnSendRequest.setBorderPainted(false);
        btnSendRequest.setPreferredSize(new Dimension(100, 30));
        btnSendRequest.addActionListener(e -> sendFriendRequest());
        buttonPanel.add(btnSendRequest);

        btnCancel = new JButton("取消");
        btnCancel.setBackground(new Color(255, 140, 160));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setPreferredSize(new Dimension(100, 30));
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnCancel);

        // 支持回车搜索
        txtSearchKeyword.addActionListener(e -> searchUsers());
    }

    /**
     * 搜索用户
     */
    private void searchUsers() {
        String keyword = txtSearchKeyword.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键词", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (parentClient != null) {
            parentClient.sendUserSearchRequest(keyword);
        }
    }

    /**
     * 更新搜索结果显示
     */
    public void updateSearchResults(String jsonResponse) {
        try {
            Gson gson = new Gson();
            // 解析服务器返回的SearchResult对象数组
            List<SearchResult> searchResultObjects = gson.fromJson(jsonResponse, new TypeToken<List<SearchResult>>() {}.getType());

            // 转换为现有的字符串数组格式
            this.searchResults = new ArrayList<>();
            if (searchResultObjects != null) {
                for (SearchResult result : searchResultObjects) {
                    this.searchResults.add(result.toStringArray());
                }
            }

            listModel.clear();
            if (searchResults != null && !searchResults.isEmpty()) {
                for (String[] user : searchResults) {
                    String displayText = formatSearchResult(user);
                    listModel.addElement(displayText);
                }
            } else {
                listModel.addElement("没有找到相关用户");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("搜索结果解析失败: " + e.getMessage());
            System.out.println("接收到的JSON: " + jsonResponse);
            listModel.clear();
            listModel.addElement("搜索结果解析失败");
        }
    }

    /**
     * 格式化搜索结果显示
     */
    private String formatSearchResult(String[] user) {
        if (user == null || user.length < 4) return "用户信息不完整";

        String userId = user[0];
        String username = user[1];
        String nickname = user[2] != null ? user[2] : "未设置";
        boolean isFriend = user.length > 3 && Boolean.parseBoolean(user[3]);

        return username + " (" + nickname + ") " + (isFriend ? "[已添加]" : "");
    }

    /**
     * 发送好友申请
     */
    private void sendFriendRequest() {
        int selectedIndex = resultList.getSelectedIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要添加的用户", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (searchResults == null || selectedIndex >= searchResults.size()) {
            JOptionPane.showMessageDialog(this, "选择的用户无效", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] selectedUser = searchResults.get(selectedIndex);
        String targetUserId = selectedUser[0];
        String targetUsername = selectedUser[1];
        String message = txtSearchResults.getText().trim();

        if (message.isEmpty()) {
            message = "请求添加您为好友";
        }

        if (parentClient != null) {
            parentClient.sendFriendRequest(targetUserId, message);
            JOptionPane.showMessageDialog(this, "好友申请已发送！", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    /**
     * 自定义搜索结果单元格渲染器
     */
    class SearchResultCellRenderer extends JLabel implements ListCellRenderer<String> {
        public SearchResultCellRenderer() {
            setOpaque(true);
            setFont(new Font("微软雅黑", Font.PLAIN, 14));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                String value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value);

            if (isSelected) {
                setBackground(new Color(255, 160, 180));
                setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
            }

            // 检查是否已经是好友
            if (searchResults != null && index < searchResults.size()) {
                String[] user = searchResults.get(index);
                if (user.length > 3 && Boolean.parseBoolean(user[3])) {
                    setIcon(createFriendIcon());
                } else {
                    setIcon(createUserIcon());
                }
            } else {
                setIcon(null);
            }

            return this;
        }

        private Icon createUserIcon() {
            // 创建普通用户图标
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(100, 149, 237)); // 蓝色
            g2d.fillOval(0, 0, 16, 16);
            g2d.dispose();
            return new ImageIcon(image);
        }

        private Icon createFriendIcon() {
            // 创建好友图标
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(76, 175, 80)); // 绿色
            g2d.fillOval(0, 0, 16, 16);
            g2d.dispose();
            return new ImageIcon(image);
        }
    }
}