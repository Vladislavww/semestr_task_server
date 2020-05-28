package bsu.rfe_g6k2.Yackou.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.swing.JFrame;

/**
 * This class sends and receives data from the clients, and processing this data
 * 
 * @version No recording 23.05.2020
 * @author Vlad Yatskou
 */
@SuppressWarnings("serial")
public class ServerClass extends JFrame implements Runnable{
	
	/** Current server's port. The port is 4512 */
	private final int SERVER_PORT = 4512;
	
	/** Container for information about current users */ 
	private ArrayList<UserClass> users;
	private String message;
	private boolean working = true;
	private AdminClass admin;
	private CryptClass crypter;
	private FileWriterClass writer;
	
	/** Contructor of ServerClass */
	public ServerClass(){
		crypter = new CryptClass();
		users = new ArrayList<UserClass>(5);
		admin = new AdminClass();
		writer = new FileWriterClass(users);
		launchThread();
	}
	
	/**
	 *  Main method of the programm.
	 *  Here is launching frame.
	 */
	public static void main(String[] args) throws IOException {
		ServerClass server = new ServerClass();
		
		server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
		server.setVisible(true);
	}

	/** Method for the thread */
	public void run() {
		try {
			writer.readDatabase();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			while (working) {
				Socket socket = serverSocket.accept();
				try {
					DataInputStream in = new DataInputStream(socket.getInputStream());
					
					/** Server's type of work on the current iteration */
					String work_type = in.readUTF();
					/** Mode of login and password */
					if (work_type.equals("CHECK_IN")) {
						String login = in.readUTF();
						String password = crypter.encryptFile(login, in.readUTF());
						String port = in.readUTF();
						message = "false";
						
						for (int i=0; i<users.size(); i++) {
							if (login.equals(users.get(i).get_login()) && password.equals(users.get(i).get_password())) {
								message = "true";
								users.get(i).set_ip(((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress());
								users.get(i).set_port(Integer.parseInt(port));
								break;
							}
						}
						String address = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
						
						Socket socket_out = new Socket(address, Integer.parseInt(port));
						
						DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
						
						out.writeUTF(work_type);
						out.writeUTF(message);
						socket_out.close();
						
					}
					
					/** Mode of registration new user */
					else if (work_type.equals("NEW_USER")) {
						String login = in.readUTF();
						String password = crypter.encryptFile(login, in.readUTF());
						String port = in.readUTF();
						String address = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
						boolean repeated = false;
						
						for (int i=0; i<users.size(); i++) {
							if (login.equals(users.get(i).get_login())) {
								repeated = true;
								break;
							}
						}
						if (repeated == false) {
							users.add(new UserClass(login, password));
							message = "created";
							writer.writeDatabase();
							writer.writeDatabase(login);
						}
						else {
							message = "not_created";
						}
						Socket socket_out = new Socket(address, Integer.parseInt(port));
						
						DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
						
						out.writeUTF(work_type);
						out.writeUTF(message);
						socket_out.close();
					}
					
					/** Mode of accepting and saving photo from client */
					else if (work_type.equals("IMPORT_PHOTO")) {
						String name = in.readUTF();
						
						String password = crypter.encryptFile(name, in.readUTF());
						
						int user_num = SearchUser(name);
						
						if (user_num>=0 && users.get(user_num).check_user(password)) {
							int bytesSize = in.readInt();
							
							byte[] bytes = new byte[bytesSize];
							
							for (int i=0; i<bytesSize; i++) {
								bytes[i] = in.readByte();
							}
							File path = writer.createPath(name);
							
							writer.saveFile(bytes, bytesSize, path);
							writer.writeDatabase(name);
						}
					}
					
					/** Mode of sending a next photo to a client */
					else if (work_type.equals("NEXT_PHOTO")) {
						String name = in.readUTF();
						
						String password = crypter.encryptFile(name, in.readUTF());
						
						int user_num = SearchUser(name);
						
						if (user_num>=0 && users.get(user_num).check_user(password)) {
							Socket socket_out = new Socket(users.get(user_num).get_ip(), users.get(user_num).get_port());
							
							DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
							File path = new File("./Database/"+name+"/"+users.get(user_num).next_photoDate()+".png");
							int bytesSize = writer.openFile_size(path);
							byte[] bytes = writer.openFile(path);
							
							out.writeUTF(work_type);
							out.writeInt(bytesSize);
							for(int i=0; i<bytesSize; i++){
								out.writeByte(bytes[i]);
							}
							socket_out.close();
						}
					}
					
					/** Mode of sending a previsious photo to a client */
					else if (work_type.equals("PREV_PHOTO")) {
						String name = in.readUTF();
						
						String password = crypter.encryptFile(name, in.readUTF());
						
						int user_num = SearchUser(name);
						
						if (user_num>=0 && users.get(user_num).check_user(password)) {
							Socket socket_out = new Socket(users.get(user_num).get_ip(), users.get(user_num).get_port());
							
							DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
							File path = new File("./Database/"+name+"/"+users.get(user_num).previsious_photoDate()+".png");
							int bytesSize = writer.openFile_size(path);
							byte[] bytes = writer.openFile(path);
							
							out.writeUTF(work_type);
							out.writeInt(bytesSize);
							for(int i=0; i<bytesSize; i++){
								out.writeByte(bytes[i]);
							}
							socket_out.close();
						}
					}
					/** Mode of deleting a current photo */
					else if (work_type.equals("DELETE_PHOTO")) {//Удаление фотографии
						String name = in.readUTF();
						
						String password = crypter.encryptFile(name, in.readUTF());
						
						int user_num = SearchUser(name);
						
						if (user_num>=0 && users.get(user_num).check_user(password)) {
							writer.deleteFile(name);
							writer.writeDatabase(name);
						}
					}
					else if (work_type.equals("CLOSE_SERVER")) {
						String name = in.readUTF();
						String password = crypter.encryptFile(name, in.readUTF());
						if (name.equals("Admin") && admin.check_admin(users.get(0).get_ip()) && users.get(0).check_user(password)) {
							working = false;
						}
					}
				}
				catch (UnknownHostException e) {
					e.printStackTrace();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				finally {
					socket.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/** Function for launching thread */
	private void launchThread(){
		Thread thread = new Thread(this);
		
		thread.start();
	}
	
	/** Searching index of user in "users" using user's name */
	private int SearchUser(String name){
		for (int i=0; i<users.size(); i++) {
			if (name.equals(users.get(i).get_login())) {
				return i;
			}
		}
		
		/**Requested name was not found */
		return -1;
	}

}
