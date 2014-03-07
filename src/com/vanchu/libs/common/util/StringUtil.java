package com.vanchu.libs.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
	public static final int BUFFER_SIZE	= 8192;
	
	public static String inputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream), BUFFER_SIZE);
		StringBuffer buffer = new StringBuffer();
		String line = "";
		while ((line = in.readLine()) != null) {
			buffer.append(line);
		}
		return buffer.toString();
	}
	
	public static String currentDateToString(String format){
		SimpleDateFormat formatter	= new SimpleDateFormat(format);
		Date currentDate	= new Date(System.currentTimeMillis());
		return formatter.format(currentDate);
	}
	
	public static String md5sum(String string) {
        byte[] hash;
        
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }
        
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        
        return hex.toString();
    }
	
	public static boolean isLegalEmail(String email) {
		String strPattern = "^[a-zA-Z0-9][a-zA-Z0-9\\.\\-_]*[a-zA-Z0-9]"
							+ "@[a-zA-Z0-9][a-zA-Z0-9\\-_]*[a-zA-Z0-9]\\."
							+ "([a-zA-Z0-9][a-zA-Z0-9\\-_]*[a-zA-Z0-9]\\.)*[a-zA-Z0-9]*$";

		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(email);
		return m.matches();
	}
	
	public static String getNameFromEmail(String email) {
		String nameAddr[]	= email.split("@");
		return nameAddr[0];
	}
	
	public static boolean isLegalPhoneNumber(String phoneNumber) {
		String strPattern = "^((13[0-9])|(14[5,7])|(15[0-9])|(18[0-9]))[0-9]{8}$";

		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(phoneNumber);
		return m.matches();
	}
}
