package com.vanchu.libs.webCache;

import com.vanchu.libs.common.util.SwitchLogger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class Network {
	
	private static final int DOWNLOAD_BUFFER_SIZE		= 2048;
    
	private int _timeout = 60000;
    
    public void setup(int timeout){
        this._timeout = timeout;
    }

    public void get(final String url, final NetworkCallback callback){
    	try {
    		URL	urlObj	= new URL(url);
			HttpURLConnection httpUrlConnection	= (HttpURLConnection)urlObj.openConnection();
			httpUrlConnection.setConnectTimeout(_timeout);
			httpUrlConnection.setReadTimeout(_timeout);

			InputStream inputStream		= httpUrlConnection.getInputStream();
			ByteArrayOutputStream tmpOutputStream	= new ByteArrayOutputStream();
			
			byte[] buffer	= new byte[DOWNLOAD_BUFFER_SIZE];
			int len		= 0;
			int hasRead	= 0;
			int total	= httpUrlConnection.getContentLength();
			int progress	= 0;
			while((len = inputStream.read(buffer)) != -1){
				tmpOutputStream.write(buffer, 0, len);
				hasRead	+= len;
				if(null != callback) {
					progress	= (int)((double)hasRead / (double)total * 100);
					callback.onProgress(url, progress);
				}
			}
			inputStream.close();
			ByteArrayInputStream resultInputStream	= new ByteArrayInputStream(tmpOutputStream.toByteArray());
			tmpOutputStream.close();
			if(null != callback) {
				callback.onSucc(url, resultInputStream);
			}
    	} catch (Exception e) {
    		SwitchLogger.e(e);
    		if(null != callback) {
    			callback.onFail(url);
    		}
    	}
    }
    
    public static class NetworkCallback {
    	public void onProgress(String url, int progress) {
    		
    	}
    	
    	public void onFail(String url) {
    		
    	}
    	
    	public void onSucc(String url, InputStream fis) {
    		
    	}
    }
}