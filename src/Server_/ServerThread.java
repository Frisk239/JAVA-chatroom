
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
	
	public ServerThread(Socket s) throws IOException {
		this.s = s;
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
				}
			} else if (command.equals("ALL")) {
							for (PrintStream ps_ : Server.clients_string.valueSet()) {
								ps_.println(content);
							}
						} else if (command.equals("DELETE")) {
				System.out.println("CLOSE!");
				// 获取要断开的用户名
				String username = str_msg[0];
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
						} else if (command.equals("USERLIST")) {
							String userlist = "";
							for (User user_ : Server.clients_string.map.keySet()) {
								String user_name = user_.getName();
								String user_ip = user_.getIp();
								userlist += ("@" + user_name + "@" + user_ip);
							}
							PrintStream ps_ = new PrintStream(s.getOutputStream(), true, "UTF-8");
			ps_.println("Server@USERLIST" + userlist);
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
