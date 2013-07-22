package com.vanchu.libs.push;

import java.util.HashMap;

import com.vanchu.libs.common.SharedPrefsUtil;
import com.vanchu.libs.common.SwitchLogger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class PushRobot {
	private static final String LOG_TAG	= PushRobot.class.getSimpleName();
	
	private static final String PREFS_PUSH_SERVICE		= "push_service";
	private static final String PREFS_PUSH_SERVICE_MSG_URL_PARAM	= "push_service_msg_url_param";
	private static final String PREFS_PUSH_SERVICE_CFG_URL_PARAM	= "push_service_cfg_url_param";
	
	public static void run(Context context, Class<?> pushServiceClass, PushParam pushParam){
		setPushParam(context, pushParam);
		
		Intent intent	= new Intent(context, pushServiceClass);
		intent.putExtra(PushService.START_TYPE, PushService.START_TYPE_INIT);
		context.startService(intent);
	}
	
	public static void check(Context context, Class<?> pushServiceClass){
		PushParam pushParam	= getPushParam(context);
		
		if(pushParam.getMsgUrl() == "" || pushParam.getCfgUrl() == ""){
			SwitchLogger.e(LOG_TAG, "push param not set, check service fail, please call run PushRobot.run() first");
			return ;
		}
		
		Intent intent	= new Intent(context, pushServiceClass);
		intent.putExtra(PushService.START_TYPE, PushService.START_TYPE_INIT);
		context.startService(intent);
	}
	
	public static PushParam getPushParam(Context context){
		SharedPreferences prefs		= context.getSharedPreferences(PREFS_PUSH_SERVICE, Context.MODE_PRIVATE);
		int		cfgInterval			= prefs.getInt("cfgInterval", PushParam.DEFAULT_CFG_INTERVAL);
		String	cfgUrl				= prefs.getString("cfgUrl", "");
		int		msgInterval			= prefs.getInt("msgInterval", PushParam.DEFAULT_MSG_INTERVAL);
		String	msgUrl				= prefs.getString("msgUrl", "");
		boolean	notifyWhenRunning	= prefs.getBoolean("notifyWhenRunning", false);
		
		HashMap<String, String> msgUrlParam	= SharedPrefsUtil.getStringMap(context, PREFS_PUSH_SERVICE_MSG_URL_PARAM);
		HashMap<String, String> cfgUrlParam	= SharedPrefsUtil.getStringMap(context, PREFS_PUSH_SERVICE_CFG_URL_PARAM);
		
		return new PushParam(msgInterval, msgUrl, msgUrlParam, cfgInterval, cfgUrl, cfgUrlParam, notifyWhenRunning);
	}
	
	public static void setPushParam(Context context, PushParam pushParam){
		SharedPreferences prefs			= context.getSharedPreferences(PREFS_PUSH_SERVICE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor	= prefs.edit();
		editor.putInt("cfgInterval", pushParam.getCfgInterval());
		editor.putString("cfgUrl", pushParam.getCfgUrl());
		editor.putInt("msgInterval", pushParam.getMsgInterval());
		editor.putString("msgUrl", pushParam.getMsgUrl());
		editor.putBoolean("notifyWhenRunning", pushParam.getNotifyWhenRunning());
		editor.commit();
		
		SharedPrefsUtil.putStringMap(context, PREFS_PUSH_SERVICE_MSG_URL_PARAM, pushParam.getMsgUrlParam());
		SharedPrefsUtil.putStringMap(context, PREFS_PUSH_SERVICE_CFG_URL_PARAM, pushParam.getCfgUrlParam());
	}
}
