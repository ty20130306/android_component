package com.vanchu.libs.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

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
}
