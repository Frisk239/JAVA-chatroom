package Client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Model.FriendRequest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 好友申请处理窗口
 */
public class FriendRequestWindow extends JDialog {
    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JList<FriendRequest> requestList;
    private DefaultListModel<FriendRequest> listModel;
    private Client parentClient;
    private JButton btnAccept;
    private JButton btnReject;
    private JButton btnClose;

    public FriendRequestWindow(Client parentClient, List<FriendRequest> pendingRequests) {
        super((Frame) null, "好友申请", true);
        this.parentClient = parentClient;

        initializeUI();
        updateRequestList(pendingRequests);

        // 添加窗口监听器，在窗口打开时自动请求数据
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent windowEvent) {
                System.out.println("好友申请窗口打开，自动请求数据...");
                if (parentClient != null) {
                    parentClient.sendFriendRequestsRequest();
                }
            }
        });
    }

    private void initializeUI() {
        setBounds(100, 100, 450, 550);  // 增加窗口高度
        setLocationRelativeTo(null);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(255, 182, 193));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // 标题
        JLabel titleLabel = new JLabel("好友申请");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setBounds(15, 10, 100, 30);
        contentPane.add(titleLabel);

        // 申请列表
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(20, 50, 410, 350);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 140, 160), 1));
        contentPane.add(scrollPane);

        listModel = new DefaultListModel<>();
        requestList = new JList<>(listModel);
        requestList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        requestList.setBackground(Color.WHITE);
        requestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestList.setCellRenderer(new FriendRequestCellRenderer());
        scrollPane.setViewportView(requestList);

        // 详情面板
        JPanel detailPanel = new JPanel();
        detailPanel.setBackground(new Color(255, 192, 203));
        detailPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 140, 160), 1));
        detailPanel.setBounds(20, 410, 410, 50);
        detailPanel.setLayout(new BorderLayout());
        contentPane.add(detailPanel);

        JTextArea detailArea = new JTextArea();
        detailArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        detailArea.setEditable(false);
        detailArea.setBackground(new Color(255, 192, 203));
        detailArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scrollPane.setViewportView(requestList);

        // 选择监听器
        requestList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailArea();
            }
        });

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 182, 193));
        buttonPanel.setBounds(20, 470, 410, 50);  // 增加按钮面板高度
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        contentPane.add(buttonPanel);

        btnAccept = new JButton("同意");
        btnAccept.setBackground(new Color(76, 175, 80)); // 绿色
        btnAccept.setForeground(Color.WHITE);
        btnAccept.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnAccept.setFocusPainted(false);
        btnAccept.setBorderPainted(false);
        btnAccept.setPreferredSize(new Dimension(100, 30));
        btnAccept.addActionListener(e -> acceptRequest());
        buttonPanel.add(btnAccept);

        btnReject = new JButton("拒绝");
        btnReject.setBackground(new Color(244, 67, 54)); // 红色
        btnReject.setForeground(Color.WHITE);
        btnReject.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnReject.setFocusPainted(false);
        btnReject.setBorderPainted(false);
        btnReject.setPreferredSize(new Dimension(100, 30));
        btnReject.addActionListener(e -> rejectRequest());
        buttonPanel.add(btnReject);

        btnClose = new JButton("关闭");
        btnClose.setBackground(new Color(255, 140, 160));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setPreferredSize(new Dimension(100, 30));
        btnClose.addActionListener(e -> dispose());
        buttonPanel.add(btnClose);
    }

    /**
     * 更新申请列表
     */
    public void updateRequestList(List<FriendRequest> requests) {
        listModel.clear();
        if (requests != null && !requests.isEmpty()) {
            for (FriendRequest request : requests) {
                listModel.addElement(request);
            }
        } else {
            listModel.addElement(new FriendRequest() {{
                setFromUserNickname("暂无待处理的申请");
                setMessage("");
            }});
        }

        updateButtonStates();
    }

    /**
     * 更新详情显示
     */
    private void updateDetailArea() {
        int selectedIndex = requestList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < listModel.getSize()) {
            FriendRequest request = listModel.getElementAt(selectedIndex);
            String detailText = String.format("申请人: %s\n申请消息: %s",
                    request.getFromUserNickname(),
                    request.getDisplayMessage());

            // 这里可以更新详情面板的显示
            System.out.println("选中申请详情: " + detailText);
        }
    }

    /**
     * 同意申请
     */
    private void acceptRequest() {
        int selectedIndex = requestList.getSelectedIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要处理的申请", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FriendRequest request = listModel.getElementAt(selectedIndex);
        if (request.getId() == 0) {
            JOptionPane.showMessageDialog(this, "无效的申请", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "确定同意 " + request.getFromUserNickname() + " 的好友申请吗？",
                "确认操作",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            if (parentClient != null) {
                parentClient.sendAcceptFriendRequest(request.getFromUserId(), request.getId());
                JOptionPane.showMessageDialog(this, "已同意好友申请！", "成功", JOptionPane.INFORMATION_MESSAGE);
                listModel.removeElementAt(selectedIndex);
                updateButtonStates();
            }
        }
    }

    /**
     * 拒绝申请
     */
    private void rejectRequest() {
        int selectedIndex = requestList.getSelectedIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要处理的申请", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FriendRequest request = listModel.getElementAt(selectedIndex);
        if (request.getId() == 0) {
            JOptionPane.showMessageDialog(this, "无效的申请", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "确定拒绝 " + request.getFromUserNickname() + " 的好友申请吗？",
                "确认操作",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            if (parentClient != null) {
                parentClient.sendRejectFriendRequest(request.getFromUserId(), request.getId());
                JOptionPane.showMessageDialog(this, "已拒绝好友申请！", "成功", JOptionPane.INFORMATION_MESSAGE);
                listModel.removeElementAt(selectedIndex);
                updateButtonStates();
            }
        }
    }

    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        boolean hasRequests = listModel.getSize() > 0;
        btnAccept.setEnabled(hasRequests && listModel.getElementAt(0).getId() > 0);
        btnReject.setEnabled(hasRequests && listModel.getElementAt(0).getId() > 0);
    }

    /**
     * 自定义申请列表单元格渲染器
     */
    class FriendRequestCellRenderer extends JPanel implements ListCellRenderer<FriendRequest> {
        private JLabel lblNickname;
        private JLabel lblMessage;
        private JLabel lblTime;

        public FriendRequestCellRenderer() {
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setOpaque(true);

            // 上半部分：昵称和时间
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            lblNickname = new JLabel();
            lblNickname.setFont(new Font("微软雅黑", Font.BOLD, 14));
            lblTime = new JLabel();
            lblTime.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            lblTime.setForeground(Color.GRAY);
            topPanel.add(lblNickname, BorderLayout.WEST);
            topPanel.add(lblTime, BorderLayout.EAST);
            add(topPanel, BorderLayout.NORTH);

            // 下半部分：消息
            lblMessage = new JLabel();
            lblMessage.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            add(lblMessage, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends FriendRequest> list,
                FriendRequest request, int index, boolean isSelected, boolean cellHasFocus) {

            if (request == null) {
                lblNickname.setText("未知申请");
                lblMessage.setText("申请信息缺失");
                lblTime.setText("");
            } else {
                lblNickname.setText(request.getFromUserNickname() != null ?
                        request.getFromUserNickname() : "未知用户");
                lblMessage.setText(request.getDisplayMessage());

                if (request.getCreatedTime() != null) {
                    lblTime.setText(request.getCreatedTime().toString().substring(0, 16));
                } else {
                    lblTime.setText("");
                }
            }

            if (isSelected) {
                setBackground(new Color(255, 160, 180));
                lblNickname.setForeground(Color.WHITE);
                lblMessage.setForeground(Color.WHITE);
                lblTime.setForeground(new Color(255, 255, 255, 180));
            } else {
                setBackground(Color.WHITE);
                lblNickname.setForeground(Color.BLACK);
                lblMessage.setForeground(Color.DARK_GRAY);
                lblTime.setForeground(Color.GRAY);
            }

            return this;
        }
    }
}