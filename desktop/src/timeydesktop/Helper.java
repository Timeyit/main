package timeydesktop;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Helper {
	
	public static String md5(String input) {
		
		String md5 = null;
		
		if(null == input) return null;
		
		try {
			
		//Create MessageDigest object for MD5
		MessageDigest digest = MessageDigest.getInstance("MD5");
		
		//Update input string in message digest
		try {
			digest.update(input.getBytes("UTF-8"), 0, input.length());
			//Converts message digest value in base 16 (hex) 
			md5 = new BigInteger(1, digest.digest()).toString(16);
			while(md5.length() < 32)
			{
				md5 = "0" + md5;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}
		return md5;
	}
}
