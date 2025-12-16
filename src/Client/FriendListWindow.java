package Client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Model.Friend;
import Model.FriendRequest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 好友列表窗口
 */
public class FriendListWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JList<String> friendList;
    private DefaultListModel<String> listModel;
    private Client parentClient;
    private JButton btnAddFriend;
    private JButton btnRefresh;
    private JButton btnFriendRequests;
    private JLabel lblTitle;
    private JButton btnClose;

    // 好友数据
    private List<Friend> friends;
    private List<FriendRequest> pendingRequests;

    public FriendListWindow(Client parentClient) {
        this.parentClient = parentClient;
        this.friends = null;
        this.pendingRequests = null;

        initializeUI();
        loadFriendData();
    }

    private void initializeUI() {
        setTitle("好友列表");
        setBounds(100, 100, 350, 600);
        setResizable(false);
        setLocationRelativeTo(parentClient);

        // 设置无边框窗口
        setUndecorated(true);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(255, 182, 193)); // 粉色背景
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // 标题栏
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(255, 160, 180)); // 深一点的粉色
        titlePanel.setBounds(0, 0, 350, 40);
        titlePanel.setLayout(null);
        contentPane.add(titlePanel);

        lblTitle = new JLabel("我的好友");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        lblTitle.setBounds(15, 8, 100, 24);
        titlePanel.add(lblTitle);

        // 关闭按钮
        btnClose = new JButton("×");
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("宋体", Font.BOLD, 18));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setBounds(310, 5, 30, 30);
        btnClose.addActionListener(e -> dispose());
        titlePanel.add(btnClose);

        // 好友列表
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(20, 60, 310, 400);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 140, 160), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPane.add(scrollPane);

        listModel = new DefaultListModel<>();
        friendList = new JList<>(listModel);
        friendList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        friendList.setBackground(Color.WHITE);
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendList.setCellRenderer(new FriendListCellRenderer());
        scrollPane.setViewportView(friendList);

        // 好友列表右键菜单
        JPopupMenu friendMenu = new JPopupMenu();
        JMenuItem itemSendMsg = new JMenuItem("发送消息");
        JMenuItem itemViewProfile = new JMenuItem("查看资料");
        JMenuItem itemDeleteFriend = new JMenuItem("删除好友");

        itemSendMsg.addActionListener(e -> sendPrivateMessage());
        itemViewProfile.addActionListener(e -> viewFriendProfile());
        itemDeleteFriend.addActionListener(e -> deleteFriend());

        friendMenu.add(itemSendMsg);
        friendMenu.add(itemViewProfile);
        friendMenu.addSeparator();
        friendMenu.add(itemDeleteFriend);

        friendList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    sendPrivateMessage();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    friendMenu.show(friendList, e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    friendMenu.show(friendList, e.getX(), e.getY());
                }
            }
        });

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 182, 193));
        buttonPanel.setBounds(20, 470, 310, 40);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        contentPane.add(buttonPanel);

        btnAddFriend = new JButton("添加好友");
        btnAddFriend.setBackground(new Color(255, 140, 160));
        btnAddFriend.setForeground(Color.WHITE);
        btnAddFriend.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnAddFriend.setFocusPainted(false);
        btnAddFriend.setBorderPainted(false);
        btnAddFriend.setPreferredSize(new Dimension(80, 30));
        btnAddFriend.addActionListener(e -> showAddFriendDialog());
        buttonPanel.add(btnAddFriend);

        btnFriendRequests = new JButton("好友申请");
        btnFriendRequests.setBackground(new Color(255, 140, 160));
        btnFriendRequests.setForeground(Color.WHITE);
        btnFriendRequests.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnFriendRequests.setFocusPainted(false);
        btnFriendRequests.setBorderPainted(false);
        btnFriendRequests.setPreferredSize(new Dimension(80, 30));
        btnFriendRequests.addActionListener(e -> showFriendRequests());
        buttonPanel.add(btnFriendRequests);

        btnRefresh = new JButton("刷新");
        btnRefresh.setBackground(new Color(255, 140, 160));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setPreferredSize(new Dimension(80, 30));
        btnRefresh.addActionListener(e -> loadFriendData());
        buttonPanel.add(btnRefresh);

        // 窗口拖拽支持
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastX = e.getXOnScreen();
                lastY = e.getYOnScreen();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();
                int deltaX = x - lastX;
                int deltaY = y - lastY;
                setLocation(getX() + deltaX, getY() + deltaY);
                lastX = x;
                lastY = y;
            }
        });
    }

    private int lastX, lastY;

    /**
     * 加载好友数据
     */
    private void loadFriendData() {
        // 向服务器请求好友列表
        if (parentClient != null) {
            parentClient.sendFriendListRequest();
            parentClient.sendFriendRequestsRequest();
        }
    }

    /**
     * 更新好友列表显示
     */
    public void updateFriendList(List<Friend> friendList) {
        System.out.println("FriendListWindow.updateFriendList 被调用");
        System.out.println("收到好友列表: " + (friendList != null ? friendList.size() : 0) + " 个好友");

        this.friends = friendList;
        listModel.clear();

        if (friendList != null && !friendList.isEmpty()) {
            for (int i = 0; i < friendList.size(); i++) {
                Friend friend = friendList.get(i);
                System.out.println("处理好友 " + i + ": " +
                    "ID=" + friend.getFriendId() +
                    ", 昵称=" + friend.getFriendNickname() +
                    ", 头像=" + friend.getFriendAvatar() +
                    ", 在线=" + friend.isOnline());

                String displayText = formatFriendDisplay(friend);
                System.out.println("显示文本: " + displayText);
                listModel.addElement(displayText);
            }
        } else {
            System.out.println("好友列表为空，显示'暂无好友'");
            listModel.addElement("暂无好友");
        }

        System.out.println("listModel 现在有 " + listModel.getSize() + " 个元素");

        // 更新标题显示好友数量
        int count = friendList != null ? friendList.size() : 0;
        lblTitle.setText("我的好友 (" + count + ")");
    }

    /**
     * 更新好友申请显示
     */
    public void updateFriendRequests(List<FriendRequest> requests) {
        this.pendingRequests = requests;
        if (requests != null && !requests.isEmpty()) {
            btnFriendRequests.setText("好友申请(" + requests.size() + ")");
        } else {
            btnFriendRequests.setText("好友申请");
        }
    }

    /**
     * 获取好友列表
     */
    public List<Friend> getFriends() {
        return this.friends;
    }

    /**
     * 格式化好友显示文本
     */
    private String formatFriendDisplay(Friend friend) {
        StringBuilder sb = new StringBuilder();
        sb.append(friend.getFriendNickname() != null ? friend.getFriendNickname() : "未知用户");

        if (friend.isOnline()) {
            sb.append(" [在线]");
        } else {
            sb.append(" [离线]");
        }

        return sb.toString();
    }

    /**
     * 发送私聊消息
     */
    private void sendPrivateMessage() {
        int selectedIndex = friendList.getSelectedIndex();
        if (selectedIndex >= 0 && friends != null && selectedIndex < friends.size()) {
            Friend selectedFriend = friends.get(selectedIndex);
            if (parentClient != null) {
                // 检查是否已经有该好友的私聊窗口打开
                PrivateChatWindow existingWindow = PrivateChatWindow.getOpenWindow(selectedFriend.getFriendId());
                if (existingWindow != null) {
                    // 如果窗口已存在，将其置前
                    existingWindow.toFront();
                    existingWindow.setState(JFrame.NORMAL);
                } else {
                    // 创建新的私聊窗口
                    PrivateChatWindow chatWindow = new PrivateChatWindow(
                        parentClient,
                        selectedFriend,
                        parentClient.getCurrentUserId()
                    );
                    chatWindow.setVisible(true);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择一个好友", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 查看好友资料
     */
    private void viewFriendProfile() {
        int selectedIndex = friendList.getSelectedIndex();
        if (selectedIndex >= 0 && friends != null && selectedIndex < friends.size()) {
            Friend selectedFriend = friends.get(selectedIndex);
            JOptionPane.showMessageDialog(this,
                    "好友昵称: " + selectedFriend.getFriendNickname() + "\n" +
                    "好友账号: " + selectedFriend.getFriendId() + "\n" +
                    "在线状态: " + (selectedFriend.isOnline() ? "在线" : "离线"),
                    "好友资料",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 删除好友
     */
    private void deleteFriend() {
        int selectedIndex = friendList.getSelectedIndex();
        if (selectedIndex >= 0 && friends != null && selectedIndex < friends.size()) {
            Friend selectedFriend = friends.get(selectedIndex);
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "确定要删除好友 " + selectedFriend.getFriendNickname() + " 吗？",
                    "删除好友",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                if (parentClient != null) {
                    parentClient.sendDeleteFriendRequest(selectedFriend.getFriendId());
                }
            }
        }
    }

    /**
     * 显示添加好友对话框
     */
    private void showAddFriendDialog() {
        AddFriendWindow addFriendWindow = new AddFriendWindow(parentClient, this);
        addFriendWindow.setVisible(true);
    }

    /**
     * 显示好友申请
     */
    private void showFriendRequests() {
        FriendRequestWindow requestWindow = new FriendRequestWindow(parentClient, pendingRequests);
        requestWindow.setVisible(true);
    }

    /**
     * 自定义好友列表单元格渲染器
     */
    class FriendListCellRenderer extends JLabel implements ListCellRenderer<String> {
        public FriendListCellRenderer() {
            setOpaque(true);
            setFont(new Font("微软雅黑", Font.PLAIN, 14));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                String value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value);

            if (isSelected) {
                setBackground(new Color(255, 160, 180)); // 深粉色
                setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
            }

            // 检查是否为在线好友
            if (friends != null && index < friends.size()) {
                Friend friend = friends.get(index);
                if (friend.isOnline()) {
                    setIcon(createOnlineIcon());
                } else {
                    setIcon(createOfflineIcon());
                }
            } else {
                setIcon(null);
            }

            return this;
        }

        private Icon createOnlineIcon() {
            // 创建在线状态图标（绿色圆点）
            BufferedImage image = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(76, 175, 80)); // 绿色
            g2d.fillOval(0, 0, 12, 12);
            g2d.dispose();
            return new ImageIcon(image);
        }

        private Icon createOfflineIcon() {
            // 创建离线状态图标（灰色圆点）
            BufferedImage image = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.GRAY);
            g2d.fillOval(0, 0, 12, 12);
            g2d.dispose();
            return new ImageIcon(image);
        }
    }
}