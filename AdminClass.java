package bsu.rfe_g6k2.Yackou.server;
import java.net.InetSocketAddress;
import java.net.Socket;

public class AdminClass{
	//private String ip = "192.168.100.8";
	private String ip = "127.0.0.1";
	public boolean check_admin(String check_ip){
		if(ip.equals(check_ip)){
			return true;
		}
		return false;
	}

}
