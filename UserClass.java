package bsu.rfe_g6k2.Yackou.server;

import java.util.LinkedList;
import java.util.ListIterator;

public class UserClass {
	private String login;
	private String password;
	private String ip;
	private int port;
	private LinkedList<String> photoDates = new LinkedList<String>();
	private ListIterator<String> iterator = photoDates.listIterator();
	
	public UserClass(String new_login, String new_password){
		login = new_login;
		password = new_password;
		ip = "";
		port = 0;
	}
	
	public void set_ip(String new_ip){
		ip = new_ip;
	}
	
	public void set_port(int new_port){
		port = new_port;
	}
	
	public String get_login(){
		return login;
	}
	
	public String get_password(){
		return password;
	}
	
	public String get_ip(){
		return ip;
	}
	
	public int get_port(){
		return port;
	}
	
	public void add_photoDate(String date){
		iterator.add(date);
	}

	public String next_photoDate(){
		if(iterator.hasNext()){
			return iterator.next();
		}
		else{
			iterator = photoDates.listIterator(0);
			if(photoDates.size()>0){
				return iterator.next();
			}
			else{
				return "null";
			}
		}
	}
	
	public String previsious_photoDate(){
		if(iterator.hasPrevious()){
			return iterator.previous();
		}
		else{
			iterator = photoDates.listIterator(photoDates.size());
			if(photoDates.size()>0){
				return iterator.previous();
			}
			else{
				return "null";
			}
		}
	}
	
	public int get_size(){
		return photoDates.size();
	}
	
	public void restart_iterator(){
		iterator = photoDates.listIterator(0);
	}
	
	public void deletePhoto(){
		iterator.remove();
	}

}