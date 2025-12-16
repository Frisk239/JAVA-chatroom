package Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

// 好友管理相关导入
import Model.Friend;
import Model.FriendRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.ArrayList;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.google.gson.Gson;

import Client.Base64Utils;
import Client.Transmission;

public class Client extends JFrame {
	/**
	 * 
	 */
	// ��������
	private static final long serialVersionUID = 6704231622520334518L;

	private static final int DEFAULT_PORT=8888;
	private static  final  String DEFAULT_IP="127.0.0.1";
	private  static final int BUFFER_SIZE=1024;
	private PlayWAV playWAV = new PlayWAV();

	private JFrame frame;
	// private JTextArea text_show;
	private JTextPane text_show;
	private JTextField txt_msg;
	private JLabel info_name;
	private JLabel info_ip;
	private JButton btn_send;
	private JButton btn_pic;
	private JButton btn_mp4_start;
	private JButton btn_mp4_stop_send;
	private JPanel northPanel;
	private JPanel southPanel;
	private JScrollPane rightScroll;
	private JScrollPane leftScroll;
	private JSplitPane centerSplit;
	private JComboBox<String> comboBox;
	private SimpleAttributeSet attrset;

	private DefaultListModel<String> listModel;
	private JList<String> userList;

	private Socket socket;
	private static PrintWriter writer; // 向server写消息
	private static BufferedReader reader; // 读server消息
	private static FileInputStream doc_read; // 读取文件
	private static FileOutputStream fos; // 写文件
	private MessageThread messageThread;// 处理消息的线程
	private Map<String, User> onLineUsers = new HashMap<String, User>();// 在线用户
	private boolean isConnected = false;
	private int port =DEFAULT_PORT ;
	private String ip = DEFAULT_IP;
	private String name;
	private String pic_path = null;
	private String mp4_path = null;
	private String UserValue = "";
	private int info_ip_ = 0;
	private int flag = 0;
	private Gson mGson;
	private boolean file_is_create = true;
	private Transmission trans;
	private AudioFormat af = null;
	private TargetDataLine td = null;
	private ByteArrayInputStream bais = null;
	private ByteArrayOutputStream baos = null;

	// 好友管理窗口
	private AddFriendWindow addFriendWindow;
	private FriendRequestWindow friendRequestWindow;
	private AudioInputStream ais = null;
	private Boolean stopflag = false;

	// 主函数入口
	public static void main(String[] args) {
		String username = "bbb";
		if (args.length > 0) {
			username = args[0];
		}
		new Client(username);
	}


	// 构造函数
	public Client(String n) {
		this.name = n;
		frame = new JFrame(name);
		frame.setVisible(true); // 可见
		frame.setBackground(Color.PINK);
		frame.setResizable(true); // 窗口大小可调整

		info_name = new JLabel(name);
		text_show = new JTextPane();
		text_show.setEditable(false);
		// text_show.setSize(300, 300);
		// text_show.setForeground(Color.BLACK);
		text_show.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));
		// text_show.setLayout(null);
		attrset = new SimpleAttributeSet();
		StyleConstants.setFontSize(attrset, 15);
		txt_msg = new JTextField();
		btn_send = new JButton("发送");
		btn_pic = new JButton("选择图片");
		btn_mp4_start = new JButton("开始录音");
		btn_mp4_stop_send = new JButton("停止&发送");
		comboBox = new JComboBox<>();
		comboBox.addItem("ALL");
		// comboBox.addItem("所有人");

		listModel = new DefaultListModel<>();
		userList = new JList<>(listModel);

		northPanel = new JPanel();
		northPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		JLabel info_a = new JLabel("UserName : ");
		info_a.setForeground(Color.WHITE);
		info_a.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		northPanel.add(info_a);
		info_name.setForeground(Color.WHITE);
		info_name.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		northPanel.add(info_name);
		TitledBorder info_b = new TitledBorder("My Info");
		info_b.setTitleColor(Color.WHITE);
		info_b.setTitleFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		northPanel.setBorder(info_b);

		rightScroll = new JScrollPane(text_show);
		TitledBorder info_c = new TitledBorder("消息");
		info_c.setTitleColor(Color.DARK_GRAY);
		info_c.setTitleFont(new Font("宋体", Font.PLAIN, 20));
		rightScroll.setBorder(info_c);
		leftScroll = new JScrollPane(userList);
		TitledBorder info_d = new TitledBorder("在线用户");
		info_d.setTitleColor(Color.DARK_GRAY);
		info_d.setTitleFont(new Font("宋体", Font.PLAIN, 20));
		leftScroll.setBorder(info_d);

		southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		
		// 消息输入区域
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BorderLayout());
		txt_msg.setBackground(Color.pink);
		btn_send.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		btn_send.setForeground(Color.DARK_GRAY);
		inputPanel.add(txt_msg, BorderLayout.CENTER);
		inputPanel.add(btn_send, BorderLayout.EAST);
		
		// 功能按钮区域
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		comboBox.setForeground(Color.DARK_GRAY);
		btn_pic.setForeground(Color.DARK_GRAY);
		btn_pic.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));
		btn_mp4_start.setForeground(Color.DARK_GRAY);
		btn_mp4_start.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));
		btn_mp4_stop_send.setForeground(Color.DARK_GRAY);
		btn_mp4_stop_send.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));
		
		// 添加聊天记录按钮
		JButton btn_save_record = new JButton("保存记录");
		btn_save_record.setForeground(Color.DARK_GRAY);
		btn_save_record.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));
		
		JButton btn_view_record = new JButton("查看记录");
		btn_view_record.setForeground(Color.DARK_GRAY);
		btn_view_record.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));
		
		// 添加字体设置按钮
			JButton btn_font = new JButton("字体设置");
			btn_font.setForeground(Color.DARK_GRAY);
			btn_font.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));
			
			// 添加窗口抖动按钮
			JButton btn_shake = new JButton("窗口抖动");
			btn_shake.setForeground(Color.DARK_GRAY);
			btn_shake.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));

			// 添加好友管理按钮
			JButton btn_friend_list = new JButton("好友列表");
			btn_friend_list.setForeground(Color.DARK_GRAY);
			btn_friend_list.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));

			JButton btn_add_friend = new JButton("添加好友");
			btn_add_friend.setForeground(Color.DARK_GRAY);
			btn_add_friend.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));

			JButton btn_friend_requests = new JButton("好友申请");
			btn_friend_requests.setForeground(Color.DARK_GRAY);
			btn_friend_requests.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));

			buttonPanel.add(comboBox);
			buttonPanel.add(btn_pic);
			buttonPanel.add(btn_mp4_start);
			buttonPanel.add(btn_mp4_stop_send);
			buttonPanel.add(btn_save_record);
			buttonPanel.add(btn_view_record);
			buttonPanel.add(btn_font);
			buttonPanel.add(btn_shake);
			buttonPanel.add(btn_friend_list);
			buttonPanel.add(btn_add_friend);
			buttonPanel.add(btn_friend_requests);
		
		// 保存聊天记录按钮事件
		btn_save_record.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveChatRecord();
			}
		});
		
		// 查看聊天记录按钮事件
		btn_view_record.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewChatRecord();
			}
		});
		
		// 字体设置按钮事件
		btn_font.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeFont();
			}
		});
		
		// 窗口抖动按钮事件
		btn_shake.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (UserValue.equals("ALL")) {
					JOptionPane.showMessageDialog(frame, "窗口抖动只能发给单个用户!");
					return;
				}
				sendMessage(frame.getTitle() + "@" + comboBox.getSelectedItem().toString() + "@" + "SHAKE" + "@" + "not");
				JOptionPane.showMessageDialog(frame, "抖动消息已发送!");
			}
		});

		// 好友列表按钮事件
		btn_friend_list.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showFriendListWindow();
			}
		});

		// 添加好友按钮事件
		btn_add_friend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 先获取好友列表，然后打开添加好友窗口
				sendFriendListRequest();
				// 创建一个临时的好友列表窗口用于添加好友
				FriendListWindow tempFriendList = new FriendListWindow(Client.this);
				addFriendWindow = new AddFriendWindow(Client.this, tempFriendList);
				addFriendWindow.setVisible(true);
			}
		});

		// 好友申请按钮事件
		btn_friend_requests.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 先获取好友申请列表
				sendFriendRequestsRequest();
				// 创建一个空的好友申请列表，窗口会自动从服务器获取最新数据
				java.util.List<Model.FriendRequest> emptyRequests = new java.util.ArrayList<>();
				friendRequestWindow = new FriendRequestWindow(Client.this, emptyRequests);
				friendRequestWindow.setVisible(true);
			}
		});
		
		// 将两个子面板添加到southPanel
		southPanel.add(inputPanel, BorderLayout.CENTER);
		southPanel.add(buttonPanel, BorderLayout.SOUTH);

		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
		centerSplit.setDividerLocation(200);

		frame.setLayout(new BorderLayout());
		northPanel.setBackground(Color.pink);
		frame.add(northPanel, BorderLayout.NORTH);
		frame.add(centerSplit, BorderLayout.CENTER);
		frame.add(southPanel, BorderLayout.SOUTH);
		frame.setSize(600, 800);
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2, (screen_height - frame.getHeight()) / 2);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		ConnectServer();// 连接服务器

		// txt_msg回车键事件
		txt_msg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ComboBoxValue();
			}
		});

		// btn_send�������Ͱ�ťʱ�¼�
		btn_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ComboBoxValue();
			}

		});
		// btn_mp4_start¼�������¼�

		btn_mp4_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				capture();
			}
		});

		// btn_mp4_stop_send����ֹͣ�����棬�����¼�
		btn_mp4_stop_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				save();
				try {
					if (mp4_path != null) {
						doc_read = new FileInputStream(mp4_path);
						sendMessage(name + "@" + "PIC_up"); // 上传图片指令
					}
					File file = new File(mp4_path);
					mGson = new Gson();
					Transmission trans = new Transmission();
					trans.transmissionType = 2;
					trans.fileName = file.getName();
					trans.fileLength = file.length();
					trans.transLength = 0;
					byte[] sendByte = new byte[1024];
					int length = 0;
					while ((length = doc_read.read(sendByte, 0, sendByte.length)) != -1) {
						trans.transLength += length;
						trans.content = Base64Utils.encode(sendByte);
						writer.write(mGson.toJson(trans) + "\r\n");
						System.out.println("上传文件进度" + 100 * trans.transLength / trans.fileLength + "%...");
														writer.flush();
												}
												System.out.println("文件上传完成");
				} catch (FileNotFoundException e1) {
																System.out.println("文件不存在");
															} catch (IOException e2) {
																System.out.println("文件写入异常");
				} finally {
					try {
						doc_read.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		// btn_pic����ͼƬ�¼�
		btn_pic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Filechose();
				try {
					if (pic_path != null) {
						doc_read = new FileInputStream(pic_path);
						sendMessage(name + "@" + "PIC_up"); // 上传图片指令
					}
					File file = new File(pic_path);
					mGson = new Gson();
					Transmission trans = new Transmission();
					trans.transmissionType = 3;
					trans.fileName = file.getName();
					trans.fileLength = file.length();
					trans.transLength = 0;
					byte[] sendByte = new byte[1024];
					int length = 0;
					while ((length = doc_read.read(sendByte, 0, sendByte.length)) != -1) {
						trans.transLength += length;
						trans.content = Base64Utils.encode(sendByte);
						writer.write(mGson.toJson(trans) + "\r\n");
						System.out.println("上传文件进度" + 100 * trans.transLength / trans.fileLength + "%...");
						writer.flush();
					}
					System.out.println("文件上传完成");
				} catch (FileNotFoundException e1) {
					System.out.println("文件不存在");
				} catch (IOException e2) {
					System.out.println("文件写入错误");
				} finally {
					try {
						doc_read.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		// 关闭窗口时事件
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isConnected) {
					try {
						// 关闭连接
						boolean flag = ConnectClose();
						if (flag == false) {
							throw new Exception("关闭连接失败!");
						} else {
							JOptionPane.showMessageDialog(frame, "成功断开!");
							txt_msg.setEnabled(false);
							btn_send.setEnabled(false);
						}
					} catch (Exception e4) {
						JOptionPane.showMessageDialog(frame, "断开连接服务器失败," + e4.getMessage(), "错误", 
								JOptionPane.ERROR_MESSAGE);
					}
				} else if (!isConnected) {
					ConnectServer();
					txt_msg.setEnabled(true);
					btn_send.setEnabled(true);
				}

			}
		});

		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				try {
					if (ItemEvent.SELECTED == evt.getStateChange()) {
						// 这里处理用户选择的情况，获取用户选择的值并保存
						String value = comboBox.getSelectedItem().toString();
						System.out.println(value);
						UserValue = value;
					}
				} catch (Exception e) {
					System.out.println("GGGFFF");
				}

			}
		});

	}

	// ���ӷ�����
	private void ConnectServer() {
		try {
			socket = new Socket(ip, port);// ���ݶ˿ںźͷ�����IP��������
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			info_ip = new JLabel(socket.getLocalAddress().toString());
			info_ip.setForeground(Color.WHITE);
			info_ip.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
			if (info_ip_ == 0) {
				northPanel.add(info_ip);
				JOptionPane.showMessageDialog(frame, name + " 连接服务器成功!");
			}
			info_ip_++;
			// ���Ϳͻ��˻�����Ϣ(�û�����IP��ַ)
			sendMessage(name + "@" + "IP" + "@" + socket.getLocalAddress().toString());
			// for(int i=0; i<100; i++);
			sendMessage(name + "@" + "ADD");
			// for(int i=0; i<100; i++);
			sendMessage(name + "@" + "USERLIST");
			// �������Ͻ�����Ϣ���߳�
			messageThread = new MessageThread(reader);
			messageThread.start();
			isConnected = true;// �Ѿ���������

			frame.setVisible(true);

		} catch (Exception e) {
			isConnected = false;// δ������
			JOptionPane.showMessageDialog(frame, "连接服务器失败," + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	// �Ͽ�����
	@SuppressWarnings("deprecation")
	public synchronized boolean ConnectClose() {
		try {

			sendMessage(name + "@" + "DELETE");// ���ͶϿ����������������
			messageThread.stop();// ֹͣ������Ϣ�߳�
			// �ͷ���Դ
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isConnected = false;
			return true;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, name + " 断开连接服务器成功!");
			isConnected = true;
			return false;
		}
	}

	// Ⱥ�ġ�˽��ѡ�񣬴����Ϣ�������б�
	public void ComboBoxValue() {
		sendMessage(name + "@" + "USERLIST");
		String message = txt_msg.getText();
		if (message == null || message.equals("")) {
			JOptionPane.showMessageDialog(frame, "消息内容为空!", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (UserValue.equals("ALL")) {
			sendMessage(frame.getTitle() + "@" + "ALL" + "@" + message + "@" + "not");

		} else {
			sendMessage(frame.getTitle() + "@" + comboBox.getSelectedItem().toString() + "@" + message + "@" + "not");
		}
		txt_msg.setText(null);
	}

	// �ļ�ѡ���������·��
	public void Filechose() {
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File(""));
		jfc.addChoosableFileFilter(new MyFileFilter());
		// jfc.
		JFrame pic_chose = new JFrame();
		pic_chose.setVisible(false);
		pic_chose.setBounds(100, 100, 800, 600);
		if (jfc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			pic_path = jfc.getSelectedFile().getAbsolutePath().toString();
			System.out.println(pic_path);
		}
	}

	// �ļ����͹���
	class MyFileFilter extends FileFilter {
		public boolean accept(File pathname) {
			if (pathname.getAbsolutePath().endsWith(".gif") || pathname.isDirectory()
					|| pathname.getAbsolutePath().endsWith(".png"))
				return true;
			return false;
		}

		public String getDescription() {
			return "ͼ���ļ�";
		}
	}

	/////////////////////////////////////////////// �������
	// ��ʼ¼��
	public void capture() {
		try {
			// afΪAudioFormatҲ������Ƶ��ʽ
			af = getAudioFormat();
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, af);
			td = (TargetDataLine) (AudioSystem.getLine(info));
			// �򿪾���ָ����ʽ���У�������ʹ�л�����������ϵͳ��Դ����ÿɲ�����
			td.open(af);
			// ����ĳһ������ִ������ I/O
			td.start();
			// ��������¼�����߳�
			Record record = new Record();
			Thread t1 = new Thread(record);
			t1.start();

		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
			return;
		}
	}

	// ֹͣ¼��
	public void stop() {
		stopflag = true;
	}

	// ����¼��
	public void save() {
		// ȡ��¼��������
		af = getAudioFormat();

		byte audioData[] = baos.toByteArray();
		bais = new ByteArrayInputStream(audioData);
		ais = new AudioInputStream(bais, af, audioData.length / af.getFrameSize());
		// �������ձ�����ļ���
		File file = null;
		// д���ļ�
		try {
			// �Ե�ǰ��ʱ������¼��������
			mp4_path = new String("");
			File filePath = new File(mp4_path);
			if (!filePath.exists()) {// ����ļ������ڣ��򴴽���Ŀ¼
				filePath.mkdir();
			}
			file = new File(filePath.getPath() + "/" + System.currentTimeMillis() + ".mp3");
			mp4_path += file.getName();
			System.out.println(mp4_path);
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// �ر���
			try {

				if (bais != null) {
					bais.close();
				}
				if (ais != null) {
					ais.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ����AudioFormat�Ĳ���
	public AudioFormat getAudioFormat() {
		// ����ע�Ͳ���������һ����Ƶ��ʽ�����߶�����
		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		float rate = 8000f;
		int sampleSize = 16;
		boolean bigEndian = true;
		int channels = 1;
		return new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);
		// //��������ÿ�벥�ź�¼�Ƶ�������
		// float sampleRate = 16000.0F;
		// // ������8000,11025,16000,22050,44100
		// //sampleSizeInBits��ʾÿ�����д˸�ʽ�����������е�λ��
		// int sampleSizeInBits = 16;
		// // 8,16
		// int channels = 1;
		// // ������Ϊ1��������Ϊ2
		// boolean signed = true;
		// // true,false
		// boolean bigEndian = true;
		// // true,false
		// return new AudioFormat(sampleRate, sampleSizeInBits, channels,
		// signed,bigEndian);
	}

	// ¼���࣬��ΪҪ�õ�MyRecord���еı��������Խ��������ڲ���
	class Record implements Runnable {
		// ������¼�����ֽ�����,��Ϊ������
		byte bts[] = new byte[10000];

		// ���ֽ������װ��������մ��뵽baos��
		// ��дrun����
		public void run() {
			baos = new ByteArrayOutputStream();
			try {
				System.out.println("ok3");
				stopflag = false;
				while (stopflag != true) {
					// ��ֹͣ¼��û����ʱ�����߳�һֱִ��
					// �������е����뻺������ȡ��Ƶ���ݡ�
					// Ҫ��ȡbts.length���ȵ��ֽ�,cnt ��ʵ�ʶ�ȡ���ֽ���
					int cnt = td.read(bts, 0, bts.length);
					if (cnt > 0) {
						baos.write(bts, 0, cnt);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					// �رմ򿪵��ֽ�������
					if (baos != null) {
						baos.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					td.drain();
					td.close();
				}
			}
		}

	}

	// 发送消息
	public static void sendMessage(String message) {
		writer.println(message);
		writer.flush();
	}
	
	// 保存聊天记录到文件
	private void saveChatRecord() {
		try {
			String content = text_show.getText();
			String fileName = "chat_record_" + name + "_" + new SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + ".txt";
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.write(content.getBytes("UTF-8"));
			fos.close();
			JOptionPane.showMessageDialog(frame, "聊天记录已保存到文件: " + fileName, "提示", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, "保存聊天记录失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			System.out.println("保存聊天记录失败: " + e.getMessage());
		}
	}
	
	// 查看聊天记录
	private void viewChatRecord() {
		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().endsWith(".txt");
				}
				public String getDescription() {
					return "文本文件 (*.txt)";
				}
			});
			
			int result = fileChooser.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), "UTF-8");
				
				// 创建一个新窗口显示聊天记录
				JFrame recordFrame = new JFrame("聊天记录 - " + file.getName());
javax.swing.JTextArea recordText = new javax.swing.JTextArea(content);
				recordText.setEditable(false);
				recordText.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));
				JScrollPane scrollPane = new JScrollPane(recordText);
				recordFrame.add(scrollPane, BorderLayout.CENTER);
				recordFrame.setSize(600, 800);
				recordFrame.setLocationRelativeTo(null);
				recordFrame.setVisible(true);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, "查看聊天记录失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			System.out.println("查看聊天记录失败: " + e.getMessage());
		}
	}
	
	// 字体设置
	private void changeFont() {
		// 使用系统字体选择对话框
		java.awt.Font currentFont = text_show.getFont();
		javax.swing.JDialog fontDialog = new javax.swing.JDialog(frame, "字体设置", true);
		fontDialog.setLayout(new java.awt.GridLayout(5, 2, 10, 10));
		fontDialog.setSize(400, 300);
		fontDialog.setLocationRelativeTo(frame);
		
		// 字体名称选择
		java.util.Vector<String> fontNames = new java.util.Vector<>();
		java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] familyNames = ge.getAvailableFontFamilyNames();
		for (String name : familyNames) {
			fontNames.add(name);
		}
		javax.swing.JComboBox<String> fontNameCombo = new javax.swing.JComboBox<>(fontNames);
		fontNameCombo.setSelectedItem(currentFont.getFamily());
		
		// 字体大小选择
		java.util.Vector<Integer> fontSizes = new java.util.Vector<>();
		for (int i = 8; i <= 36; i += 2) {
			fontSizes.add(i);
		}
		javax.swing.JComboBox<Integer> fontSizeCombo = new javax.swing.JComboBox<>(fontSizes);
		fontSizeCombo.setSelectedItem(currentFont.getSize());
		
		// 字体样式选择
		javax.swing.JCheckBox boldCheck = new javax.swing.JCheckBox("粗体");
		boldCheck.setSelected((currentFont.getStyle() & java.awt.Font.BOLD) != 0);
		
		javax.swing.JCheckBox italicCheck = new javax.swing.JCheckBox("斜体");
		italicCheck.setSelected((currentFont.getStyle() & java.awt.Font.ITALIC) != 0);
		
		// 确定和取消按钮
		javax.swing.JButton okBtn = new javax.swing.JButton("确定");
		javax.swing.JButton cancelBtn = new javax.swing.JButton("取消");
		
		// 添加组件到对话框
		fontDialog.add(new javax.swing.JLabel("字体:"));
		fontDialog.add(fontNameCombo);
		fontDialog.add(new javax.swing.JLabel("大小:"));
		fontDialog.add(fontSizeCombo);
		fontDialog.add(new javax.swing.JLabel("样式:"));
		fontDialog.add(new javax.swing.JPanel()); // 占位
		fontDialog.add(boldCheck);
		fontDialog.add(italicCheck);
		fontDialog.add(okBtn);
		fontDialog.add(cancelBtn);
		
		// 确定按钮事件
		okBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				// 获取选中的字体属性
				String fontName = (String) fontNameCombo.getSelectedItem();
				int fontSize = (Integer) fontSizeCombo.getSelectedItem();
				int fontStyle = java.awt.Font.PLAIN;
				if (boldCheck.isSelected()) {
					fontStyle |= java.awt.Font.BOLD;
				}
				if (italicCheck.isSelected()) {
					fontStyle |= java.awt.Font.ITALIC;
				}
				
				// 创建新字体
				java.awt.Font newFont = new java.awt.Font(fontName, fontStyle, fontSize);
				
				// 应用到聊天窗口
				text_show.setFont(newFont);
				
				// 更新属性集，确保新消息也使用新字体
				StyleConstants.setFontFamily(attrset, fontName);
				StyleConstants.setFontSize(attrset, fontSize);
				StyleConstants.setBold(attrset, (fontStyle & java.awt.Font.BOLD) != 0);
				StyleConstants.setItalic(attrset, (fontStyle & java.awt.Font.ITALIC) != 0);
				
				fontDialog.dispose();
			}
		});
		
		// 取消按钮事件
		cancelBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				fontDialog.dispose();
			}
		});
		
		fontDialog.setVisible(true);
	}
	
	// 窗口抖动实现
	private void shakeWindow() {
		int originalX = frame.getLocation().x;
		int originalY = frame.getLocation().y;
		
		// 抖动效果：上下左右移动窗口
		for (int i = 0; i < 5; i++) {
			try {
				// 向右上移动
				frame.setLocation(originalX + 10, originalY - 10);
				Thread.sleep(50);
				// 向左下移动
				frame.setLocation(originalX - 10, originalY + 10);
				Thread.sleep(50);
				// 向左上移动
				frame.setLocation(originalX - 10, originalY - 10);
				Thread.sleep(50);
				// 向右下移动
				frame.setLocation(originalX + 10, originalY + 10);
				Thread.sleep(50);
				// 回到原位
				frame.setLocation(originalX, originalY);
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// ==================== 好友管理功能 ====================

	/**
	 * 发送用户搜索请求
	 */
	public void sendUserSearchRequest(String keyword) {
		sendMessage(name + "@USER_SEARCH@" + keyword);
	}

	/**
	 * 发送好友申请
	 */
	public void sendFriendRequest(String targetUserId, String message) {
		sendMessage(name + "@FRIEND_ADD@" + targetUserId + "@" + message);
	}

	/**
	 * 发送获取好友列表请求
	 */
	public void sendFriendListRequest() {
		sendMessage(name + "@FRIEND_LIST@");
	}

	/**
	 * 发送获取好友申请请求
	 */
	public void sendFriendRequestsRequest() {
		sendMessage(name + "@FRIEND_REQUESTS@");
	}

	/**
	 * 同意好友申请
	 */
	public void sendAcceptFriendRequest(String fromUserId, int requestId) {
		sendMessage(name + "@FRIEND_ACCEPT@" + fromUserId + "@" + requestId);
	}

	/**
	 * 拒绝好友申请
	 */
	public void sendRejectFriendRequest(String fromUserId, int requestId) {
		sendMessage(name + "@FRIEND_REJECT@" + fromUserId + "@" + requestId);
	}

	/**
	 * 删除好友
	 */
	public void sendDeleteFriendRequest(String friendId) {
		sendMessage(name + "@FRIEND_DELETE@" + friendId);
	}

	/**
	 * 设置私聊目标
	 */
	public void setPrivateChatTarget(String targetNickname) {
		UserValue = targetNickname;
		comboBox.setSelectedItem(targetNickname);
		JOptionPane.showMessageDialog(frame, "已开始与 " + targetNickname + " 的私聊", "私聊提示", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * 显示好友列表窗口
	 */
	public void showFriendListWindow() {
		FriendListWindow friendListWindow = new FriendListWindow(this);
		friendListWindow.setVisible(true);
	}

	// 消息接收线程

	// ------------------------------------------------------------------------------------
	// ���Ͻ�����Ϣ���߳�
	class MessageThread extends Thread {
		private BufferedReader reader;

		// ������Ϣ�̵߳Ĺ��췽��
		public MessageThread(BufferedReader reader) {
			this.reader = reader;
		}

		@SuppressWarnings("unlikely-arg-type")
		public void run() {
			String message = "";
			while (true) {
				try {
					if (flag == 0) {
						message = reader.readLine();
						// 添加null检查，防止NullPointerException
						if (message == null || message.trim().isEmpty()) {
							continue;
						}
						StringTokenizer stringTokenizer = new StringTokenizer(message, "@");
						// ��������Ϣ����
						String[] str_msg = new String[10];
						int j_ = 0;
						while (stringTokenizer.hasMoreTokens()) {
							str_msg[j_++] = stringTokenizer.nextToken();
						}
						String command = str_msg[1];// 指令
						// �������ѹر��ź�
						if (command.equals("SERVERClOSE")) {
							Document docs = text_show.getDocument();
							try {
								docs.insertString(docs.getLength(), "服务器已关闭!\r\n", attrset);// 向文本区域输出
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
							// text_show.add("服务器已关闭!\r\n", null);
							closeCon();// 关闭连接
							return;// 结束线程
						}
						// 处理用户列表指令
						else if (command.equals("ADD")) {
							String username = "";
							String userIp = "";
							username = str_msg[0];
							userIp = socket.getLocalAddress().toString();
							User user = new User(username, userIp);
							onLineUsers.put(username, user);
							listModel.addElement(username);
							comboBox.addItem(username);
						}
						// 处理用户列表指令
						else if (command.equals("DELETE")) {
							String username = str_msg[0];
							User user = (User) onLineUsers.get(username);
							onLineUsers.remove(user);
							listModel.removeElement(username);
							comboBox.removeItem(username);
						}
						// �����û��б�
						else if (command.equals("USERLIST")) {
							String username = null;
							String userIp = null;
							for (int i = 2; i < str_msg.length; i += 2) {
								if (str_msg[i] == null)
									break;
								username = str_msg[i];
								userIp = str_msg[i + 1];
								User user = new User(username, userIp);
								onLineUsers.put(username, user);
								if (listModel.contains(username))
									;
								else
									listModel.addElement(username);
								int len = comboBox.getItemCount();
								int _i = 0;
								for (; _i < len; _i++) {
									if (comboBox.getItemAt(_i).toString().equals(username))
										break;
								}
								if (_i == len)
									comboBox.addItem(username);
								else
									;
							}
						}
						// 达到最大容量消息
						else if (command.equals("MAX")) {
							closeCon();// 关闭连接
							JOptionPane.showMessageDialog(frame, "已经达到最大容量,请稍后再试!", "提示", JOptionPane.CANCEL_OPTION);
							return;// 返回主线程
						}
						// 群聊消息
						else if (command.equals("ALL")) {
							SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
							String time = df.format(new java.util.Date());
							Document docs = text_show.getDocument();
							try {
								docs.insertString(docs.getLength(),
										"[" + time + "]\r\n" + str_msg[0] + " 说 : " + str_msg[2] + "\r\n\n", attrset);// 向文本区域输出
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
							// text_show.add(, null);// 普通消息
							playWAV.Play("sounds/msg.wav");
						}
						// 私聊消息
				else if (command.equals("ONLY")) {
					SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
					String time = df.format(new java.util.Date());
					Document docs = text_show.getDocument();
					try {
						docs.insertString(docs.getLength(), "[" + time + "]\r\n" + str_msg[0] + " 悄悄对你说 : " + str_msg[2] + "\r\n\n",
							attrset);// 向文本区域输出
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					// text_show.add(, null);// 普通消息
					playWAV.Play("sounds/msg.wav");
				}
				// 窗口抖动
				else if (command.equals("SHAKE")) {
					shakeWindow();
					Document docs = text_show.getDocument();
					try {
						docs.insertString(docs.getLength(), str_msg[0] + " 给你发送了一个窗口抖动!\r\n\n", attrset);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					playWAV.Play("sounds/msg.wav");
				}
						// ==================== 好友管理功能处理 ====================
						// 用户搜索结果
						else if (command.equals("USER_SEARCH_RESULT")) {
							// 更新AddFriendWindow的搜索结果
							if (addFriendWindow != null && addFriendWindow.isShowing()) {
								String searchResultsJson = str_msg[2];
								addFriendWindow.updateSearchResults(searchResultsJson);
								System.out.println("已更新搜索结果: " + searchResultsJson);
							} else {
								System.out.println("AddFriendWindow不存在或未显示");
							}
						}
						// 好友列表响应
						else if (command.equals("FRIEND_LIST_RESULT")) {
							try {
								Gson gson = new Gson();
								String friendListJson = str_msg[2];
								List<Friend> friendList = gson.fromJson(friendListJson, new TypeToken<List<Friend>>() {}.getType());
								// 更新好友列表窗口
								// 需要与FriendListWindow实例关联
							} catch (Exception e) {
								System.out.println("解析好友列表失败: " + e.getMessage());
							}
						}
						// 好友申请列表响应
						else if (command.equals("FRIEND_REQUESTS_RESULT")) {
							try {
								Gson gson = new Gson();
								String requestsJson = str_msg[2];
								System.out.println("收到好友申请JSON: " + requestsJson);
								System.out.println("JSON长度: " + requestsJson.length());

								List<FriendRequest> requests = gson.fromJson(requestsJson, new TypeToken<List<FriendRequest>>() {}.getType());
								System.out.println("JSON解析成功，解析到 " + requests.size() + " 条申请");

								// 更新好友申请窗口
								if (friendRequestWindow != null && friendRequestWindow.isShowing()) {
									friendRequestWindow.updateRequestList(requests);
									System.out.println("已更新好友申请列表: " + requests.size() + " 条申请");
								} else {
									System.out.println("好友申请窗口未显示或为null");
								}
							} catch (Exception e) {
								System.out.println("解析好友申请列表失败: " + e.getMessage());
								System.out.println("完整错误信息: " + e.getClass().getSimpleName() + ": " + e.getMessage());
								e.printStackTrace();
							}
						}
						// 好友申请处理结果
						else if (command.equals("FRIEND_ADD_RESULT")) {
							boolean success = Boolean.parseBoolean(str_msg[2]);
							String resultMsg = str_msg.length > 3 ? str_msg[3] : "";
							if (success) {
								JOptionPane.showMessageDialog(frame, "好友申请发送成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(frame, "好友申请发送失败：" + resultMsg, "失败", JOptionPane.ERROR_MESSAGE);
							}
						}
						// 好友申请接受/拒绝结果
						else if (command.equals("FRIEND_PROCESS_RESULT")) {
							boolean success = Boolean.parseBoolean(str_msg[2]);
							String resultMsg = str_msg.length > 3 ? str_msg[3] : "";
							if (success) {
								JOptionPane.showMessageDialog(frame, "好友申请处理成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
								// 刷新好友申请列表
								sendFriendRequestsRequest();
							} else {
								JOptionPane.showMessageDialog(frame, "好友申请处理失败：" + resultMsg, "失败", JOptionPane.ERROR_MESSAGE);
							}
						}
						// 处理图片
						else if (command.equals("PIC_up_ok")) {
							sendMessage(name + "@" + "PIC_down");
							flag = 1;
							// break;
						}
						str_msg = null; // 消息清空
					} // if(flag == 0)
					else if (flag == 1) {
						System.out.println("客户端准备接收消息");

						mGson = new Gson();
						while ((message = reader.readLine()) != null) {
							trans = mGson.fromJson(message, Transmission.class);
							long fileLength = trans.fileLength;
							long transLength = trans.transLength;
							if (file_is_create) {
								fos = new FileOutputStream(new File(
										"" + trans.fileName));
								file_is_create = false;
							}
							byte[] b = Base64Utils.decode(trans.content.getBytes());
							fos.write(b, 0, b.length);
							System.out.println("接收文件进度" + 100 * transLength / fileLength + "%...");
							if (transLength == fileLength) {
								file_is_create = true;
								fos.flush();
								fos.close();
								if (trans.fileName.endsWith(".jpg")) {
									ImageIcon icon = new ImageIcon(
											"" + trans.fileName);
									// icon.
									SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
									String time = df.format(new java.util.Date());
									StyledDocument doc = text_show.getStyledDocument();
									Document docs = text_show.getDocument();
									try {
										docs.insertString(docs.getLength(),
											"[" + time + "]\r\n" + name + " 说 : " + "\r\n", attrset);// 向文本区域输出
										text_show.setCaretPosition(doc.getLength());
										text_show.insertIcon(icon);
										docs = text_show.getDocument();
										docs.insertString(docs.getLength(), "\r\n", attrset);
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
								} else if (trans.fileName.endsWith(".mp3")) {
									SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
									String time = df.format(new java.util.Date());
									Document docs = text_show.getDocument();
									try {
										docs.insertString(docs.getLength(),
												"[" + time + "]\r\n" + name + " 发了一段语音 : " + "\r\n\n", attrset);// 向文本区域输出
										playWAV.Play("" + trans.fileName);
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
								}
								break;
							}
						}
						System.out.println("文件接收完成");
						flag = 0;
					} /// else if
				} // try
				catch (IOException e1) {
					// ConnectServer();
					e1.printStackTrace();
					System.out.println("客户端接收消息线程 run() e1:" + e1.getMessage());
					break;
				} catch (Exception e2) {
					// ConnectServer();
					e2.printStackTrace();
					System.out.println("客户端接收消息线程 run() e2:" + e2.getMessage());
					break;
				}
			} // while
		} // run

		// ������ֹͣ�󣬿ͻ��˹ر����ӡ�
		// synchronized关键字是一个对象锁，当一个线程使用时能够保证同一时间只有一个线程执行该方法。
		public synchronized void closeCon() throws Exception {
			listModel.removeAllElements();// 清空用户列表
			// 按顺序关闭资源并释放资源
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isConnected = false;// 修改状态为关闭
		}
	}
}