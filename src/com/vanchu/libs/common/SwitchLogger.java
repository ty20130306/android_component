package com.vanchu.libs.common;

import android.util.Log;

public class SwitchLogger {
	
	private static boolean _printLog	= false; 
	
	public static void d(String tag, String msg){
		if(isPrintLog()){
			Log.d(tag, msg);
		}
	}
	
	public static void w(String tag, String msg){
		if(isPrintLog()){
			Log.w(tag, msg);
		}
	}
	
	public static void e(String tag, String msg){
		if(isPrintLog()){
			Log.e(tag, msg);
		}
	}
	
	public static void e(Exception e){
		if(isPrintLog()){
			e.printStackTrace();
		}
	}
	
	public static void setPrintLog(boolean printLog){
		SwitchLogger._printLog		= printLog;
	}
	
	public static boolean isPrintLog(){
		return SwitchLogger._printLog;
	}
}
