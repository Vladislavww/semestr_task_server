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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class FileWriterClass {
	private ArrayList<UserClass> users;
	
	public FileWriterClass(ArrayList<UserClass> new_users){
		users = new_users;
	}
	
	//функция записи записи в файл логинов и паролей
	public void writeDatabase() throws IOException{ 
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
	public void writeDatabase(String name) throws IOException{ 
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
	public void readDatabase() throws IOException{ 
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
	private int SearchUser(String name){
		for(int i=0; i<users.size(); i++){
			if(name.equals(users.get(i).get_login())){
				return i;
			}
		}
		return -1;
	}
	
	//Загрузка фотографии со своей БД
	public byte[] openFile(File selectedFile){
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
	public int openFile_size(File selectedFile){
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
	public void saveFile(byte[] bytesFigure, int bytesFigureSize, File path){
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
	public void deleteFile(String name){
		int user_num = SearchUser(name);
		users.get(user_num).previsious_photoDate();
		File path = new File("./Database/"+name+"/"+users.get(user_num).next_photoDate()+".png");
		users.get(user_num).deletePhoto();
		path.delete();
	}
	
	//Создание файла-пути
	public File createPath(String name){
		File path = new File("./Database/"+name);
		path.mkdir();
		int user_num = SearchUser(name);
		String photoName = createPhotoName();
		path = new File("./Database/"+name+"/"+photoName+".png");
		users.get(user_num).add_photoDate(photoName);
		return path;
	}
	
	//Создание имени фотографии в зависимости от текущего времени
	public String createPhotoName(){
		Date date = new Date();
		SimpleDateFormat name = new SimpleDateFormat("yyyyMMddHHmmss");
		return name.format(date);
	}

}
