package bsu.rfe_g6k2.Yackou.server;

/**
 * This class checks IP of user that uses name "Admin".
 * 
 * @version No recording 23.05.2020
 * @author Vlad Yatskou
 */
public class AdminClass {
	
	/** IP that server's administrator has */
	private final static String adminIp = "127.0.0.1";
	
	/** Method that checks IP of user that uses name "Admin". */
	public boolean check_admin(String ipToCheck) {
		return adminIp.equals(ipToCheck);
	}
}