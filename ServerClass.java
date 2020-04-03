package bsu.rfe_g6k2.Yackou.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;


public class ServerClass {
	private static final int SERVER_PORT = 4512;
	private static ArrayList<UserClass> users = new ArrayList<UserClass>(5);
	private static String message;
	private static boolean working = true;
	private static AdminClass admin = new AdminClass();
	
	//функция записи записи в файл логинов и паролей
	private static void writeDatabase() throws IOException{ 
		FileWriter output_logins = new FileWriter("Logins.txt");
		FileWriter output_passwords = new FileWriter("Passwords.txt");
		for(int i=0; i<users.size(); i++){
			output_logins.write(users.get(i).get_login()+'\n');
			output_passwords.write(users.get(i).get_password()+'\n'); 
		}
		output_logins.close();
		output_passwords.close();
	}
	
	//функция записи записи в файл имен фотографий пользователя
	private static void writeDatabase(String name) throws IOException{ 
		FileWriter output_dates = new FileWriter(name+"_Inf.txt");
		int user_num = SearchUser(name);
		int size = users.get(user_num).get_size();
		users.get(user_num).restart_iterator();
		for(int i=0; i<size; i++){
			output_dates.write(users.get(user_num).next_photoDate()+'\n');
		}
		output_dates.close();
	}
	
	//функция чтения файла
	private static void readDatabase() throws IOException{ 
		FileReader input_logins = new FileReader("Logins.txt");
		FileReader input_passwords = new FileReader("Passwords.txt");
		Scanner scan_logins = new Scanner(input_logins);
		Scanner scan_passwords = new Scanner(input_passwords);
		int i = 0;
		while(scan_logins.hasNextLine()&&scan_passwords.hasNextLine()){
			String name = scan_logins.nextLine();
			users.add(new UserClass(name, scan_passwords.nextLine()));
			FileReader input_dates = new FileReader(name+"_Inf.txt");
			Scanner scan_dates = new Scanner(input_dates);
			while(scan_dates.hasNextLine()){
				users.get(i).add_photoDate(scan_dates.nextLine());
			}
			input_dates.close();
			i += 1;
		}
		input_logins.close();
		input_passwords.close();
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
	
	//Загрузка фотографии со своей БД
	private static byte[] openFile(File selectedFile){
		byte[] bytesFigure = null;
		try { 
			DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
			bytesFigure = new byte[in.available()];
			int bytesFigureSize = in.available();
			int i=0;
			while (in.available()>0){
				bytesFigure[i] = in.readByte();
				i += 1;
			}
			in.close(); 
		} 
		catch (FileNotFoundException ex){ 
		} 
		catch (IOException ex){ 
		}
		finally{
			return bytesFigure;
		}
	}
	
	//Чтение размера фотографии 
	private static int openFile_size(File selectedFile){
		int bytesFigureSize = 0;
		try { 
			DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
			bytesFigureSize = in.available();
			in.close(); 
		} 
		catch (FileNotFoundException ex){ 
		} 
		catch (IOException ex){ 
		}
		finally{
			return bytesFigureSize;
		}
	}
	
	//Сохранение фотографии в свою БД
	private static void saveFile(byte[] bytesFigure, int bytesFigureSize, File path){
		try {  
			DataOutputStream out = new DataOutputStream(new FileOutputStream(path));
			for(int i=0; i<bytesFigureSize; i++){
				out.writeByte(bytesFigure[i]);
			}
			out.close();
		} 
		catch (Exception e) {
		}
	}
	
	//Удаление фотографии из БД
	private static void deleteFile(String name){
		int user_num = SearchUser(name);
		File path = new File("./Database/"+name+"/"+users.get(user_num).next_photoDate()+".png");
		users.get(user_num).deletePhoto();
		path.delete();
	}
	
	//Создание файла-пути
	private static File createPath(String name){
		File path = new File("./Database/"+name);
		path.mkdir();
		int user_num = SearchUser(name);
		String photoName = createPhotoName();
		path = new File("./Database/"+name+"/"+photoName+".png");
		users.get(user_num).add_photoDate(photoName);
		return path;
	}
	
	//Создание имени фотографии в зависимости от текущего времени
	private static String createPhotoName(){
		Date date = new Date();
		SimpleDateFormat name = new SimpleDateFormat("yyyyMMddHHmmss");
		return name.format(date);
	}
	public static void main(String[] args) throws IOException {
		readDatabase();
		final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
		try{
			while(working){
				final Socket socket = serverSocket.accept();
				try{
					final DataInputStream in = new DataInputStream(socket.getInputStream());
					final String work_type = in.readUTF(); //режим работы сервера на данной итерации
					if(work_type.equals("CHECK_IN")){//режим проверки логина и пароля
						final String login = in.readUTF();
						final String password = in.readUTF();
						final String port = in.readUTF();
						message = "false";
						for(int i=0; i<users.size(); i++){
							if(login.equals(users.get(i).get_login()) && password.equals(users.get(i).get_password())){
								message = "true";
								users.get(i).set_ip(((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress());
								users.get(i).set_port(Integer.parseInt(port));
								users.get(i).set_online(true);
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
						final String password = in.readUTF();
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
							writeDatabase();
							writeDatabase(login);
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
						int bytesSize = in.readInt();
						int user_num = SearchUser(name);
						if(user_num>=0 && users.get(user_num).get_online()){
							byte[] bytes = new byte[bytesSize];
							for(int i=0; i<bytesSize; i++){
								bytes[i] = in.readByte();
							}
							File path = createPath(name);
							saveFile(bytes, bytesSize, path);
							writeDatabase(name);
						}
					}
					else if(work_type.equals("NEXT_PHOTO")){//Отправка клиенту следующей фотографии
						final String name = in.readUTF();
						int user_num = SearchUser(name);
						if(user_num>=0 && users.get(user_num).get_online()){
							final Socket socket_out = new Socket(users.get(user_num).get_ip(), users.get(user_num).get_port());
							final DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
							File path = new File("./Database/"+name+"/"+users.get(user_num).next_photoDate()+".png");
							int bytesSize = openFile_size(path);
							byte[] bytes = openFile(path);
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
						int user_num = SearchUser(name);
						if(user_num>=0 && users.get(user_num).get_online()){
							final Socket socket_out = new Socket(users.get(user_num).get_ip(), users.get(user_num).get_port());
							final DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
							File path = new File("./Database/"+name+"/"+users.get(user_num).previsious_photoDate()+".png");
							int bytesSize = openFile_size(path);
							byte[] bytes = openFile(path);
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
						int user_num = SearchUser(name);
						if(user_num>=0 && users.get(user_num).get_online()){
							deleteFile(name);
							writeDatabase(name);
						}
					}
					else if(work_type.equals("CLOSE_SERVER")){
						final String name = in.readUTF();
						if(name.equals("Admin") && admin.check_admin(users.get(0).get_ip())){
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
		}
		finally{
			serverSocket.close();
		}

	}

}
