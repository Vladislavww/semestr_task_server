package bsu.rfe_g6k2.Yackou.server;

import java.util.Random;

public class CryptClass {
	public String encryptFile(String key, String toEncrypt){
		int sum = 0;
		for(int i=0; i<key.length(); i++){
			sum += key.charAt(i);
		}
		Random random = new Random(sum);
		int num;
		char[] toreturn = new char[toEncrypt.length()];
		for(int i=0; i<toEncrypt.length(); i++){
			num = toEncrypt.charAt(i);
			num -= random.nextInt()%256;
			toreturn[i] = (char)num;
		}
		return new String(toreturn);
	}

}
