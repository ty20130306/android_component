package com.vanchu.libs.push;

import java.util.HashMap;

import com.vanchu.libs.common.SharedPrefsUtil;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class PushRobot {
	private static final String PREFS_PUSH_SERVICE		= "push_service";
	private static final String PREFS_PUSH_SERVICE_MSG_URL_PARAM	= "push_service_msg_url_param";
	
	public static void run(Context context, Class<?> pushServiceClass, PushParam pushParam){
		setPushParam(context, pushParam);
		
		Intent intent	= new Intent(context, pushServiceClass);
		intent.putExtra(PushService.START_TYPE, PushService.START_TYPE_INIT);
		context.startService(intent);
	}
	
	public static void check(Context context, Class<?> pushServiceClass){
		Intent intent	= new Intent(context, pushServiceClass);
		intent.putExtra(PushService.START_TYPE, PushService.START_TYPE_INIT);
		context.startService(intent);
	}
	
	public static PushParam getPushParam(Context context){
		SharedPreferences prefs		= context.getSharedPreferences(PREFS_PUSH_SERVICE, Context.MODE_PRIVATE);
		int		msgInterval			= prefs.getInt("msgInterval", PushParam.DEFAULT_MSG_INTERVAL);
		String	msgUrl				= prefs.getString("msgUrl", "");
		boolean	notifyWhenRunning	= prefs.getBoolean("notifyWhenRunning", false);
		int 	defaults			= prefs.getInt("defaults", Notification.DEFAULT_LIGHTS);
		
		HashMap<String, String> msgUrlParam	= SharedPrefsUtil.getStringMap(context, PREFS_PUSH_SERVICE_MSG_URL_PARAM);
		
		PushParam pushParam	= new PushParam(msgInterval, msgUrl, msgUrlParam);
		pushParam.setNotifyWhenRunning(notifyWhenRunning);
		pushParam.setDefaults(defaults);
		
		return pushParam;
	}
	
	public static void setPushParam(Context context, PushParam pushParam){
		SharedPreferences prefs			= context.getSharedPreferences(PREFS_PUSH_SERVICE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor	= prefs.edit();
		editor.putInt("msgInterval", pushParam.getMsgInterval());
		editor.putString("msgUrl", pushParam.getMsgUrl());
		editor.putBoolean("notifyWhenRunning", pushParam.getNotifyWhenRunning());
		editor.putInt("defaults", pushParam.getDefaults());
		editor.commit();
		
		SharedPrefsUtil.putStringMap(context, PREFS_PUSH_SERVICE_MSG_URL_PARAM, pushParam.getMsgUrlParam());
	}
}
