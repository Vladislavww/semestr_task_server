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


@SuppressWarnings("serial")
public class ServerClass extends JFrame implements Runnable{
	private static final int SERVER_PORT = 4512;
	private static ArrayList<UserClass> users;
	private static String message;
	private static boolean working = true;
	private static AdminClass admin;
	private static CryptClass crypter;
	private static FileWriterClass writer;
	
	public ServerClass(){
		crypter = new CryptClass();
		users = new ArrayList<UserClass>(5);
		admin = new AdminClass();
		writer = new FileWriterClass(users);
		Thread thread = new Thread(this);
		thread.start();
	}
	
	//Функция поиска номера пользователя по его имени
	private static int SearchUser(String name){
		for(int i=0; i<users.size(); i++){
			if(name.equals(users.get(i).get_login())){
				return i;
			}
		}
		return -1;
	}
	
	
	public static void main(String[] args) throws IOException {
		ServerClass server = new ServerClass();
		server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
		server.setVisible(true);
	}

	public void run() {
		try {
			writer.readDatabase();
		} 
		catch (IOException e1){}
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e1){}
		try{
			while(working){
				final Socket socket = serverSocket.accept();
				try{
					final DataInputStream in = new DataInputStream(socket.getInputStream());
					final String work_type = in.readUTF(); //режим работы сервера на данной итерации
					if(work_type.equals("CHECK_IN")){//режим проверки логина и пароля
						final String login = in.readUTF();
						final String password = crypter.encryptFile(login, in.readUTF());
						final String port = in.readUTF();
						message = "false";
						for(int i=0; i<users.size(); i++){
							if(login.equals(users.get(i).get_login()) && password.equals(users.get(i).get_password())){
								message = "true";
								users.get(i).set_ip(((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress());
								users.get(i).set_port(Integer.parseInt(port));
								break;
							}
						}
						final String address = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
						final Socket socket_out = new Socket(address, Integer.parseInt(port));
						final DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
						out.writeUTF(work_type);
						out.writeUTF(message);
						socket_out.close();
						
					}
					else if(work_type.equals("NEW_USER")){ //режим регистрации новой уч.записи
						final String login = in.readUTF();
						final String password = crypter.encryptFile(login, in.readUTF());
						final String port = in.readUTF();
						final String address = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
						boolean repeated = false;
						for(int i=0; i<users.size(); i++){
							if(login.equals(users.get(i).get_login())){
								repeated = true;
								break;
							}
						}
						if(repeated == false){
							users.add(new UserClass(login, password));
							message = "created";
							writer.writeDatabase();
							writer.writeDatabase(login);
						}
						else{
							message = "not_created";
						}
						final Socket socket_out = new Socket(address, Integer.parseInt(port));
						final DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
						out.writeUTF(work_type);
						out.writeUTF(message);
						socket_out.close();
					}
					else if(work_type.equals("IMPORT_PHOTO")){//режим принятия и сохранения фотографии от клиента
						final String name = in.readUTF();
						final String password = crypter.encryptFile(name, in.readUTF());
						int user_num = SearchUser(name);
						if(user_num>=0 && users.get(user_num).check_user(password)){
							int bytesSize = in.readInt();
							byte[] bytes = new byte[bytesSize];
							for(int i=0; i<bytesSize; i++){
								bytes[i] = in.readByte();
							}
							File path = writer.createPath(name);
							writer.saveFile(bytes, bytesSize, path);
							writer.writeDatabase(name);
						}
					}
					else if(work_type.equals("NEXT_PHOTO")){//Отправка клиенту следующей фотографии
						final String name = in.readUTF();
						final String password = crypter.encryptFile(name, in.readUTF());
						int user_num = SearchUser(name);
						if(user_num>=0 && users.get(user_num).check_user(password)){
							final Socket socket_out = new Socket(users.get(user_num).get_ip(), users.get(user_num).get_port());
							final DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
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
					else if(work_type.equals("PREV_PHOTO")){//Отправка клиенту предыдущей фотографии
						final String name = in.readUTF();
						final String password = crypter.encryptFile(name, in.readUTF());
						int user_num = SearchUser(name);
						if(user_num>=0 && users.get(user_num).check_user(password)){
							final Socket socket_out = new Socket(users.get(user_num).get_ip(), users.get(user_num).get_port());
							final DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
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
					else if(work_type.equals("DELETE_PHOTO")){//Удаление фотографии
						final String name = in.readUTF();
						final String password = crypter.encryptFile(name, in.readUTF());
						int user_num = SearchUser(name);
						if(user_num>=0 && users.get(user_num).check_user(password)){
							writer.deleteFile(name);
							writer.writeDatabase(name);
						}
					}
					else if(work_type.equals("CLOSE_SERVER")){
						final String name = in.readUTF();
						final String password = crypter.encryptFile(name, in.readUTF());
						if(name.equals("Admin") && admin.check_admin(users.get(0).get_ip()) && users.get(0).check_user(password)){
							working = false;
						}
					}
				}
				catch (UnknownHostException e){
					e.printStackTrace();
					System.out.println("Была ошибка. Работа продолжена.");
				} 
				catch (IOException e){
					e.printStackTrace();
					System.out.println("Была ошибка. Работа продолжена.");
				}
				finally{
					socket.close();
				}
			}
		} catch (IOException e) {
		}
		finally{
			try {
				serverSocket.close();
			} catch (IOException e) {}
		}
		
	}

}
