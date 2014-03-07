package com.vanchu.libs.common.util;

import android.util.Log;

public class SwitchLogger {
	
	private static boolean _printLog	= false; 
	
	public static void d(String tag, String msg){
		if(isPrintLog()){
			if(null == msg) {
				Log.d(tag, "null");
			} else {
				Log.d(tag, msg);
			}
		}
	}
	
	public static void w(String tag, String msg){
		if(isPrintLog()){
			if(null == msg) {
				Log.w(tag, "null");
			} else {
				Log.w(tag, msg);
			}
		}
	}
	
	public static void e(String tag, String msg){
		if(isPrintLog()){
			if(null == msg) {
				Log.e(tag, "null");
			} else {
				Log.e(tag, msg);
			}
		}
	}
	
	public static void e(Exception e){
		if(isPrintLog() && null != e){
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
