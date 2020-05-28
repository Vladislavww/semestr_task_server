package bsu.rfe_g6k2.Yackou.server;

import java.util.Random;

/**
 * This class encrypts string variables
 * 
 * @version No recording 23.05.2020
 * @author Vlad Yatskou
 */
public class CryptClass {
	
	/**Function that password (toEncrypt) with help of user's login (key) */
	public String encryptFile(String key, String toEncrypt) {
		
		if ((key != null) && (toEncrypt != null)){
			int sum = 0;
			
			for(int i=0; i<key.length(); i++) {
				sum += key.charAt(i);
			}
			
			/** random is a variable for getting pseudorandom number */
			Random random = new Random(sum);
			int num;
			char[] toreturn = new char[toEncrypt.length()];
			
			for(int i=0; i<toEncrypt.length(); i++) {
				num = toEncrypt.charAt(i);
				num -= random.nextInt()%256;
				toreturn[i] = (char)num;
			}
			return new String(toreturn);
		}
		return null;
	}

}
