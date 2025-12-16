
package Server_;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import com.google.gson.Gson;

import Server_.Transmission;
import Server_.Base64Utils;
import Model.Friend;
import Model.FriendRequest;

public class ServerThread extends Thread {
	Socket s = null;
	BufferedReader br = null;
	PrintStream ps = null;
	User user = null;
	FileOutputStream fos = null;
	DataOutputStream doc_write = null; // 向client写文件
	FileInputStream doc_read = null; // 读取文件
	Gson mGson;
	Transmission trans;
	int flag = 0;
	int file_length;
	String file_name_just = null;
	boolean file_is_create = true;
	boolean client_rec_first = true;

	// 好友管理器
	private FriendManager friendManager;
	private String currentUserId; // 当前用户的ID
	
	public ServerThread(Socket s) throws IOException {
		this.s = s;
		this.friendManager = FriendManager.getInstance();
	}
	//按目录文件时间排序
	 public static List<File> getFileSort(String path) {
	        List<File> list = getFiles(path, new ArrayList<File>());
	        if (list != null && list.size() > 0) {
	            Collections.sort(list, new Comparator<File>() {
	                public int compare(File file, File newFile) {
	                    if (file.lastModified() < newFile.lastModified()) {
	                        return 1;
	                    } else if (file.lastModified() == newFile.lastModified()) {
	                        return 0;
	                    } else {
	                        return -1;
	                    }
	                }
	            });
	        }
	        return list;
	    }
	 //获取指定目录文件
	 public static List<File> getFiles(String realpath, List<File> files) {
	        File realFile = new File(realpath);
	        if (realFile.isDirectory()) {
	            File[] subfiles = realFile.listFiles();
	            for (File file : subfiles) {
	                if (file.isDirectory()) {
	                    getFiles(file.getAbsolutePath(), files);
	                } else {
	                    files.add(file);
	                }
	            }
	        }
	        return files;
	    }
	
	public void run() {
		try {
			String content = null;
			br = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
			ps = new PrintStream(s.getOutputStream(), true, "UTF-8");

			while (true) {
				if (flag == 0) {
					while ((content = readFromClient()) != null) {
						flag = 0;
						int user_list = Server.clients_string.valueSet().size();
						System.out.println("Msg_from_Client : " + content);

						StringTokenizer stringTokenizer = new StringTokenizer(content, "@");
						String[] str_msg = new String[10];
						int j_ = 0;
						while (stringTokenizer.hasMoreTokens()) {
							str_msg[j_++] = stringTokenizer.nextToken();
						}
						String command = str_msg[1];// 指令

						if (command.equals("IP")) {
				System.out.println(str_msg[0] + " " + str_msg[1] + " " + str_msg[2] + " ********* ");
				// 检查用户名是否已存在
				boolean userExists = false;
				for (User existingUser : Server.clients_string.map.keySet()) {
					if (existingUser.getName().equals(str_msg[0])) {
						userExists = true;
						break;
					}
				}
				if (userExists) {
					// 用户名已存在，发送错误消息
					ps.println("Server@ERROR@User " + str_msg[0] + " is already online!");
					// 关闭连接
					try {
						if (br != null) br.close();
						if (ps != null) ps.close();
						if (s != null) s.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					return;
				} else {
					user = new User(str_msg[0], str_msg[2]);
					// 设置当前用户ID（暂时使用用户名作为ID，后续可以从数据库获取真实的user_id）
					currentUserId = str_msg[0];
					System.out.println("用户登录: " + currentUserId);
				}
			} else if (command.equals("ALL")) {
							for (PrintStream ps_ : Server.clients_string.valueSet()) {
								ps_.println(content);
							}
						} else if (command.equals("DELETE")) {
				System.out.println("CLOSE!");
				// 获取要断开的用户名
				String username = str_msg[0];

				// 清除用户的好友管理缓存
				if (friendManager != null && currentUserId != null) {
					friendManager.updateUserOnlineStatus(currentUserId, false);
					friendManager.clearUserCache(currentUserId);
					System.out.println("清除用户缓存: " + currentUserId);
				}

				// 广播用户下线通知
				for (PrintStream ps_ : Server.clients_string.valueSet()) {
					ps_.println(username + "@DELETE");
				}
				Server.clients_string.removeByValue(ps);
				try {
					if (br != null) {
						br.close();
					}
					if (ps != null) {
						ps.close();
					}
					if (s != null) {
						s.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			} else if (command.equals("ADD")) {
							Server.clients_string.put(user, ps);
							// 更新用户在线状态
							if (friendManager != null && currentUserId != null) {
								friendManager.updateUserOnlineStatus(currentUserId, true);
								System.out.println("用户上线: " + currentUserId);
							}
						} else if (command.equals("USERLIST")) {
							String userlist = "";
							for (User user_ : Server.clients_string.map.keySet()) {
								String user_name = user_.getName();
								String user_ip = user_.getIp();
								userlist += ("@" + user_name + "@" + user_ip);
							}
							PrintStream ps_ = new PrintStream(s.getOutputStream(), true, "UTF-8");
			ps_.println("Server@USERLIST" + userlist);
						} else if (command.equals("FRIEND_ADD")) {
							// 好友申请：格式：发送者@FRIEND_ADD@目标用户@申请消息
							String targetUser = str_msg[2];
							String message = str_msg.length > 3 ? str_msg[3] : "请求添加您为好友";
							System.out.println("好友申请: " + currentUserId + " -> " + targetUser);

							boolean success = friendManager.sendFriendRequest(currentUserId, targetUser, message);
							if (success) {
								ps.println("Server@FRIEND_ADD_SUCCESS@" + targetUser);

								// 通知目标用户（如果在线）
								for (User onlineUser : Server.clients_string.map.keySet()) {
									if (onlineUser.getName().equals(targetUser)) {
										PrintStream targetPs = Server.clients_string.map.get(onlineUser);
										targetPs.println("Server@FRIEND_REQUEST@" + currentUserId + "@" + message);
										break;
									}
								}
							} else {
								ps.println("Server@FRIEND_ADD_FAILED@" + targetUser);
							}

						} else if (command.equals("FRIEND_ACCEPT")) {
							// 同意好友申请：格式：发送者@FRIEND_ACCEPT@申请人ID@申请ID
							String fromUserId = str_msg[2];
							int requestId = Integer.parseInt(str_msg[3]);
							System.out.println("同意好友申请: " + fromUserId + " -> " + currentUserId);

							boolean success = friendManager.processFriendRequest(requestId, fromUserId, currentUserId, 1);
							if (success) {
								ps.println("Server@FRIEND_ACCEPT_SUCCESS@" + fromUserId);

								// 通知对方（如果在线）
								for (User onlineUser : Server.clients_string.map.keySet()) {
									if (onlineUser.getName().equals(fromUserId)) {
										PrintStream targetPs = Server.clients_string.map.get(onlineUser);
										targetPs.println("Server@FRIEND_ACCEPTED@" + currentUserId);
										break;
									}
								}
							} else {
								ps.println("Server@FRIEND_ACCEPT_FAILED");
							}

						} else if (command.equals("FRIEND_REJECT")) {
							// 拒绝好友申请：格式：发送者@FRIEND_REJECT@申请人ID@申请ID
							String fromUserId = str_msg[2];
							int requestId = Integer.parseInt(str_msg[3]);
							System.out.println("拒绝好友申请: " + fromUserId + " -> " + currentUserId);

							boolean success = friendManager.processFriendRequest(requestId, fromUserId, currentUserId, 2);
							if (success) {
								ps.println("Server@FRIEND_REJECT_SUCCESS@" + fromUserId);

								// 通知对方（如果在线）
								for (User onlineUser : Server.clients_string.map.keySet()) {
									if (onlineUser.getName().equals(fromUserId)) {
										PrintStream targetPs = Server.clients_string.map.get(onlineUser);
										targetPs.println("Server@FRIEND_REJECTED@" + currentUserId);
										break;
									}
								}
							} else {
								ps.println("Server@FRIEND_REJECT_FAILED");
							}

						} else if (command.equals("FRIEND_LIST")) {
							// 获取好友列表：格式：发送者@FRIEND_LIST
							System.out.println("获取好友列表: " + currentUserId);

							String friendListJson = friendManager.getFriendListJson(currentUserId);
							ps.println("Server@FRIEND_LIST_RESULT@" + friendListJson);

						} else if (command.equals("FRIEND_REQUESTS")) {
							// 获取待处理的好友申请：格式：发送者@FRIEND_REQUESTS
							System.out.println("获取好友申请: " + currentUserId);

							String requestsJson = friendManager.getPendingRequestsJson(currentUserId);
							System.out.println("发送好友申请JSON: " + requestsJson);
							ps.println("Server@FRIEND_REQUESTS_RESULT@" + requestsJson);
							System.out.println("已发送FRIEND_REQUESTS_RESULT消息");

						} else if (command.equals("FRIEND_DELETE")) {
							// 删除好友：格式：发送者@FRIEND_DELETE@好友ID
							String friendId = str_msg[2];
							System.out.println("删除好友: " + currentUserId + " -> " + friendId);

							boolean success = friendManager.deleteFriend(currentUserId, friendId);
							if (success) {
								ps.println("Server@FRIEND_DELETE_SUCCESS@" + friendId);

								// 通知对方（如果在线）
								for (User onlineUser : Server.clients_string.map.keySet()) {
									if (onlineUser.getName().equals(friendId)) {
										PrintStream targetPs = Server.clients_string.map.get(onlineUser);
										targetPs.println("Server@FRIEND_DELETED@" + currentUserId);
										break;
									}
								}
							} else {
								ps.println("Server@FRIEND_DELETE_FAILED@" + friendId);
							}

						} else if (command.equals("USER_SEARCH")) {
							// 搜索用户：格式：发送者@USER_SEARCH@关键词
							String keyword = str_msg[2];
							System.out.println("搜索用户: " + currentUserId + " 关键词: " + keyword);

							String searchResultsJson = friendManager.getSearchResultsJson(keyword, currentUserId);
							System.out.println("发送搜索结果JSON: " + searchResultsJson);
							ps.println("Server@USER_SEARCH_RESULT@" + searchResultsJson);
							System.out.println("已发送USER_SEARCH_RESULT消息");

						} else if (command.equals("PRIVATE_CHAT")) {
							// 私聊消息：格式：发送者@PRIVATE_CHAT@目标用户ID@消息类型@消息内容
							String targetUserId = str_msg[2];
							String messageType = str_msg[3];
							String messageContent = str_msg.length > 4 ? str_msg[4] : "";
							System.out.println("私聊消息: " + currentUserId + " -> " + targetUserId + " 类型:" + messageType);

							// 验证好友关系
							if (!friendManager.areFriends(currentUserId, targetUserId)) {
								ps.println("Server@ERROR@不是好友关系，无法发送私聊消息");
								break;
							}

							// 保存消息到数据库
							boolean saved = friendManager.savePrivateMessage(currentUserId, targetUserId, messageType, messageContent);
							if (!saved) {
								ps.println("Server@ERROR@消息保存失败");
								break;
							}

							// 查找目标用户是否在线
							boolean targetOnline = false;
							for (User onlineUser : Server.clients_string.map.keySet()) {
								if (onlineUser.getName().equals(targetUserId)) {
									targetOnline = true;
									PrintStream targetPs = Server.clients_string.map.get(onlineUser);

									// 转发私聊消息给目标用户
									String privateMessage = currentUserId + "@PRIVATE_MESSAGE@" + messageType + "@" + messageContent;
									targetPs.println(privateMessage);
									System.out.println("已转发私聊消息给在线用户: " + targetUserId);
									break;
								}
							}

							if (!targetOnline) {
								System.out.println("目标用户离线，消息已保存到数据库");
								ps.println("Server@INFO@消息已发送，对方离线时将保存到服务器");
							} else {
								ps.println("Server@INFO@消息已发送");
							}

						} else if (command.equals("PRIVATE_FILE")) {
							// 私聊文件：格式：发送者@PRIVATE_FILE@目标用户ID@文件名@文件大小
							String targetUserId = str_msg[2];
							String fileName = str_msg[3];
							long fileSize = Long.parseLong(str_msg[4]);
							System.out.println("私聊文件: " + currentUserId + " -> " + targetUserId + " 文件:" + fileName);

							// 验证好友关系
							if (!friendManager.areFriends(currentUserId, targetUserId)) {
								ps.println("Server@ERROR@不是好友关系，无法发送文件");
								break;
							}

							// 设置文件传输标志
							flag = 2; // 使用2表示私聊文件传输
							file_name_just = fileName;
							file_length = (int) fileSize;

							// 通知客户端准备接收文件
							ps.println("Server@PRIVATE_FILE_READY@" + targetUserId + "@" + fileName + "@" + fileSize);

						} else if (command.equals("MARK_MESSAGES_READ")) {
							// 标记消息为已读：格式：发送者@MARK_MESSAGES_READ@发送者ID
							String fromUserId = str_msg[2];
							System.out.println("标记消息已读: " + fromUserId + " -> " + currentUserId);

							boolean marked = friendManager.markMessagesAsRead(fromUserId, currentUserId);
							if (marked) {
								ps.println("Server@INFO@消息已标记为已读");
							} else {
								ps.println("Server@ERROR@标记消息已读失败");
							}

						} else if (command.equals("GET_UNREAD_COUNT")) {
							// 获取未读消息数量：格式：发送者@GET_UNREAD_COUNT
							System.out.println("获取未读消息数量: " + currentUserId);

							int unreadCount = friendManager.getUnreadMessageCount(currentUserId);
							ps.println("Server@UNREAD_COUNT@" + unreadCount);

						} else if (user_list > 20) {
							PrintStream ps_ = new PrintStream(s.getOutputStream(), true, "UTF-8");
			ps_.println("Server@MAX");
						} else if (command.equals("PIC_up")) {
							flag = 1;
							break;
						} else if (command.equals("PIC_down")) {
							System.out.println("服务器接收客户端的文件上传成功，准备下载文件");
							try {
								file_name_just = getFileSort("").get(0).getName();
								String doc_path = new String("" + file_name_just);
								doc_read = new FileInputStream(doc_path);
								File file = new File(doc_path);
								mGson = new Gson();
								Transmission trans = new Transmission();
								trans.transmissionType = 3;
								trans.fileName = file.getName();
								trans.fileLength = file.length();
								trans.transLength = 0;
for (PrintStream ps_ : Server.clients_string.valueSet()) {
						byte[] sendByte = new byte[1024];
						int length = 0;
						while ((length = doc_read.read(sendByte, 0, sendByte.length)) != -1) {
							trans.transLength += length;
							trans.content = Base64Utils.encode(sendByte);
							ps_.println(mGson.toJson(trans));
							System.out.println("下载文件进度" + 100 * trans.transLength / trans.fileLength + "%...");
							ps_.flush();
						}
						// 重置已传输长度，以便下一个客户端接收完整文件
						trans.transLength = 0;
						// 重置文件输入流，以便重新读取文件
						doc_read.close();
						doc_read = new FileInputStream(doc_path);
					}
								System.out.println("Server发送完毕");
							}
							catch (FileNotFoundException e1){
								System.out.println("文件不存在");
							}
							catch (IOException e2) {
								System.out.println("文件写入错误");
							} 
							finally {
								try {
									doc_read.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
							// break;
						} else {
							User user_ss = null;
							for (User user_ : Server.clients_string.map.keySet())
								if (user_.getName().equals(command)) {
									user_ss = user_;
									break;
								}
							if (user_ss != null) {
								System.out.println("The whisper msg!");
								if (str_msg[2].equals("SHAKE")) {
									// 处理窗口抖动消息
									Server.clients_string.map.get(user_ss)
										.println(Server.clients_string.getKeyByValue(ps).getName() + "@" + "SHAKE");
								} else {
									// 处理普通私聊消息
									Server.clients_string.map.get(user_ss)
										.println(Server.clients_string.getKeyByValue(ps).getName() + "@" + "ONLY" + "@" + str_msg[2]);
								}
							} else {
								// 目标用户不存在，向发送者返回错误信息
								System.out.println("Target user not found: " + command);
								ps.println("Server@ERROR@User " + command + " is not online!");
							}
						}
					} // while
				} // if
				else if (flag == 1) {
						mGson = new Gson();
						while ((content = readFromClient()) != null) {
							trans = mGson.fromJson(content, Transmission.class);
							long fileLength = trans.fileLength;
							long transLength = trans.transLength;
							file_name_just = trans.fileName;
							if(file_is_create) {
								fos = new FileOutputStream(
										new File("" + trans.fileName));
								file_is_create = false;
							}
							byte[] b = Base64Utils.decode(trans.content.getBytes());
							fos.write(b, 0, b.length);
							System.out.println("上传文件进度" + 100 * transLength / fileLength + "%...");
							if (transLength == fileLength) {
								file_is_create = true;
								fos.flush();
								fos.close();
								break;
							}
						}
						System.out.println("上传文件完成");
						for (PrintStream ps_ : Server.clients_string.valueSet()) {
							ps_.println("Server@PIC_up_ok");
						}
						flag = 0;
					} // else if
					
				} // while
		} // try
		catch (IOException e1) {
			System.out.println("文件写入错误 : ServerThread线程 run() e:" + e1.getMessage());
			Server.clients_string.removeByValue(ps);
			try {
				if (br != null) {
					br.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (s != null) {
					s.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} catch (Exception e3) {
			try {
				System.out.println(e3.getMessage());
				if (s != null) {
					s.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public String readFromClient() {
		try {
			return br.readLine();
		} catch (IOException e) {
			Server.clients_string.removeByValue(ps);
		}
		return null;
	}
}
