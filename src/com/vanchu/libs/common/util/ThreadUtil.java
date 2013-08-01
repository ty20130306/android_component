package com.vanchu.libs.common.util;

public class ThreadUtil {
	
	public static void sleep(long millis){
		try{
			Thread.sleep(millis);
		} catch (InterruptedException e){
			SwitchLogger.e(e);
		}
	}
}
